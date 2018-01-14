package com.example.yooas.websocketchatter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by yooas on 2018-01-14.
 */
class MessageAdapter(val messages: ArrayList<Message> = ArrayList()): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemChanged(messages.indexOf(message))
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.setMessage(message.mMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_message, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(var view: View): RecyclerView.ViewHolder(view) {
        val mMessageView = view.findViewById<TextView>(R.id.id_message)

        fun setMessage(message: String) {
            mMessageView.text = message
        }
    }
}