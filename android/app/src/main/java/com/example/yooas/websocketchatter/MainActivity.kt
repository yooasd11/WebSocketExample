package com.example.yooas.websocketchatter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    lateinit var mSocket: Socket
    var mAdapter: MessageAdapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSocket = IO.socket("http://192.168.219.103:3000")
        mSocket.connect()
        mSocket.on("message", { args ->
            this@MainActivity.runOnUiThread({
                var data: JSONObject = args[0] as JSONObject
                var message: String
                try {
                    message = data.getString("message").toString()
                    mAdapter.addMessage(Message.Builder().type(0).message(message).build())
                } catch (e: Exception) {
                    return@runOnUiThread
                }
            })
        })

        id_button.setOnClickListener { view ->
            sendMessage()
        }
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
}
