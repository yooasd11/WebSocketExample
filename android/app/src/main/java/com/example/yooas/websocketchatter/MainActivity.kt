package com.example.yooas.websocketchatter

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.example.yooas.websocketchatter.UtilsKotlin.Companion.decodeStringToFile
import com.example.yooas.websocketchatter.UtilsKotlin.Companion.encodeFileToString
import com.example.yooas.websocketchatter.UtilsKotlin.Companion.playWavFile
import com.example.yooas.websocketchatter.UtilsKotlin.Companion.startRecording
import com.example.yooas.websocketchatter.UtilsKotlin.Companion.stopRecording
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var mSocket: Socket
    private lateinit var mAdapter: MessageAdapter
    private var mIsRecording = false
    private var mRecordedAudioPath: String = ""
    private var mReceivedAudioPath: String = ""

    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.map { permission -> {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
                    }
                }
            }
        }

        var sd = Environment.getExternalStorageDirectory().absolutePath
        mRecordedAudioPath = sd + "/record_recorded.pcm"
        mReceivedAudioPath = sd + "/record_received.pcm"

        mAdapter = MessageAdapter(receivedAudioPath = mReceivedAudioPath)

        id_button_connect.setOnClickListener(this)
        id_button_send.setOnClickListener(this)
        id_button_record.setOnClickListener(this)
        id_button_play.setOnClickListener(this)
        id_button_send_record.setOnClickListener(this)

        id_recycler.layoutManager = LinearLayoutManager(this)
        id_recycler.adapter = mAdapter

        mHandler = object: Handler() {
            override fun handleMessage(msg: android.os.Message) {
                val size = msg.what.toFloat() / 200
                id_circle.resize(size)
            }
        }
    }

    private fun sendMessage(type: Int, message: String) {
        val data = JSONObject()
        data.put("type", type)
        data.put("content", message)
        mSocket.emit("message", data)
    }

    private fun addMessage(message: String, type: Int) {
        mAdapter.addMessage(Message.Builder().type(type).message(message).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.id_button_connect -> {
                val ip = id_edit_server_ip.text.toString()
                mSocket = IO.socket("http://$ip:3000")
                mSocket.connect()
                mSocket.on("message", { args ->
                    this@MainActivity.runOnUiThread({
                        val data: JSONObject = args[0] as JSONObject
                        try {
                            val type = data.getInt("type")
                            val message = data.getString("content")
                            when (type) {
                                0 -> addMessage(message, type)
                                1 -> {
                                    decodeStringToFile(message, mReceivedAudioPath)
                                    addMessage(message, type)
                                }
                            }
                        } catch (e: Exception) {
                            return@runOnUiThread
                        }
                    })
                })
            }
            R.id.id_button_record -> {
                if (!mIsRecording) {
                    mIsRecording = true
                    startRecording(mRecordedAudioPath, mHandler)
                    id_button_record.text = "STOP"
                    id_button_play.alpha = 0.1f
                    id_button_play.isClickable = false
                } else {
                    mIsRecording = false
                    stopRecording()
                    id_button_record.text = "RECORD"
                    id_button_play.alpha = 1.0f
                    id_button_play.isClickable = true
                }
            }
            R.id.id_button_play -> {
                if (mRecordedAudioPath.isEmpty() || mIsRecording) return
                playWavFile(mRecordedAudioPath)
            }
            R.id.id_button_send -> sendMessage(0, id_edit.text.toString())
            R.id.id_button_send_record -> {
                sendMessage(1, encodeFileToString(mRecordedAudioPath))
            }
        }
    }

}
