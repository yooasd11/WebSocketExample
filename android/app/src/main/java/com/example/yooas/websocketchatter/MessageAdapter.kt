package com.example.yooas.websocketchatter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

/**
 * Created by yooas on 2018-01-14.
 */
class MessageAdapter(val messages: ArrayList<Message> = ArrayList(), val receivedAudioPath: String): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemChanged(messages.indexOf(message))
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        when (message.mType) {
            0 -> holder.setMessage(message.mMessage)
            1 -> holder.setAudio()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message, parent, false)
        return ViewHolder(view, receivedAudioPath)
    }

    class ViewHolder(var view: View, val receivedAudioPath: String): RecyclerView.ViewHolder(view) {
        val mMessageView = view.findViewById<TextView>(R.id.id_message)
        val mPlayButton = view.findViewById<Button>(R.id.id_button_play_received)

        fun setMessage(message: String) {
            mPlayButton.visibility = View.GONE
            mMessageView.text = message
        }

        fun setAudio() {
            mMessageView.visibility = View.GONE
            mPlayButton.setOnClickListener { view ->
                UtilsKotlin.playWavFile(receivedAudioPath)
            }
        }
    }
}