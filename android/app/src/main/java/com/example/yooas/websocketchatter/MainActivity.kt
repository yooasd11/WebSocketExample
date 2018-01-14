package com.example.yooas.websocketchatter

import android.media.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var mSocket: Socket

    private var mAdapter: MessageAdapter = MessageAdapter()

    private val mSampleRates = arrayOf(44100, 22050, 11025, 8000)
    private val mAudioFormats = arrayOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_DEFAULT)
    private val mChannelConfigs = arrayOf(AudioFormat.CHANNEL_IN_STEREO, AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_DEFAULT)
    private val mBufferSize = 1024

    private val mBytesPerElement = 2
    private var mRecorder: AudioRecord? = null

    private var mIsRecording = false
    private var mSampleRate: Int = -1

    private var mAudioFormat: Int = -1
    private var mChannelConfig: Int = -1
    private var mPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSocket = IO.socket("http://192.168.219.104:3000")
        mSocket.connect()
        mSocket.on("message", { args ->
            this@MainActivity.runOnUiThread({
                val data: JSONObject = args[0] as JSONObject
                val message: String
                try {
                    message = data.getString("message").toString()
                    addMessage(message)
                } catch (e: Exception) {
                    return@runOnUiThread
                }
            })
        })

        id_button_send.setOnClickListener(this)
        id_button_record.setOnClickListener(this)
        id_button_play.setOnClickListener(this)

        id_recycler.layoutManager = LinearLayoutManager(this)
        id_recycler.adapter = mAdapter
    }

    private fun sendMessage() {
        val message = id_edit.text.toString()
        id_edit.setText("")
        mSocket.emit("message", message)
    }

    private fun addMessage(message: String) {
        mAdapter.addMessage(Message.Builder().type(0).message(message).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.id_button_record -> {
                if (!mIsRecording) {
                    startRecording()
                    id_button_record.text = "STOP"
                    id_button_play.alpha = 0.1f
                    id_button_play.isClickable = false
                } else {
                    stopRecording()
                    id_button_record.text = "RECORD"
                    id_button_play.alpha = 1.0f
                    id_button_play.isClickable = true
                }
            }
            R.id.id_button_play -> {
                if (mPath.isEmpty() || mIsRecording) return
                playWavFile()
            }
            R.id.id_button_send -> sendMessage()
        }
    }

    private fun playWavFile() {
        val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelConfig, mAudioFormat)
        val audioTrack = AudioTrack(AudioManager.STREAM_VOICE_CALL, mSampleRate, mChannelConfig, mAudioFormat, minBufferSize, AudioTrack.MODE_STREAM)
        val data = ByteArray(minBufferSize)
        var count = 0

        val fis = FileInputStream(mPath)
        val dis = DataInputStream(fis)
        audioTrack.play()
        while (true) {
            count = dis.read(data, 0, minBufferSize)
            if (count <= -1) break
            audioTrack.write(data, 0, count)
        }
        audioTrack.stop()
        audioTrack.release()
        dis.close()
        fis.close()

    }

    private fun stopRecording() {
        if (mRecorder == null) return
        mIsRecording = false
        mRecorder!!.stop()
        mRecorder!!.release()
    }

    private fun startRecording() {
        mRecorder = findAudioRecord()
        mRecorder!!.startRecording()
        Thread({
            writeAudioDataToFile()
        }).start()
        mIsRecording = true
    }

    private fun writeAudioDataToFile() {
        var sd = Environment.getExternalStorageDirectory().absolutePath
        mPath = sd + "/record.pcm"

        val data = ShortArray(mBufferSize)

        var fos: FileOutputStream = FileOutputStream(mPath)
        while (mIsRecording) {
            mRecorder!!.read(data, 0, mBufferSize)
            val bData = Util.short2byte(data)
            fos.write(bData, 0, mBufferSize * mBytesPerElement)
        }
        fos.close()
    }

    private fun findAudioRecord(): AudioRecord? {
        for (rate in mSampleRates) {
            for (format in mAudioFormats) {
                for (channel in mChannelConfigs) {
                    val bufferSize = AudioRecord.getMinBufferSize(rate, channel, format)
                    if (bufferSize == AudioRecord.ERROR_BAD_VALUE) continue
                    mSampleRate = rate
                    mAudioFormat = format
                    mChannelConfig = channel

                    val recorder = AudioRecord(MediaRecorder.AudioSource.DEFAULT, mSampleRate, mChannelConfig, mAudioFormat, bufferSize)
                    if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                        return recorder
                    }
                }
            }
        }
        return null
    }
}
