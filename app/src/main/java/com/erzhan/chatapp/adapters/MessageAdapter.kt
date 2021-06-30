package com.erzhan.chatapp.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Message
import com.google.firebase.auth.FirebaseAuth


class MessageAdapter(
    context: Context,
    messages: List<Message>,
    onItemCLickListener: OnItemClickListener
) : RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    private val messageList: List<Message>
    private val inflater: LayoutInflater
    private var onItemClickListener: OnItemClickListener

    init {
        this.messageList = messages
        inflater = LayoutInflater.from(context)
        this.onItemClickListener = onItemCLickListener
    }

    class MyViewHolder(itemView: View, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextViewId)
        private var onItemClickListener: OnItemClickListener

        init {
            this.onItemClickListener = onItemClickListener
            itemView.setOnClickListener { v: View ->
                onClick(
                    v
                )
            }
        }

        fun bind(message: Message) {
            messageTextView.text = message.text
            val myUserId = FirebaseAuth.getInstance().uid
            if (message.senderId == myUserId){
                val layout: LinearLayout.LayoutParams = messageTextView.layoutParams as LinearLayout.LayoutParams
                layout.gravity = Gravity.END
                messageTextView.layoutParams = layout
            }
        }

        override fun onClick(v: View) {
            onItemClickListener.onItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_message, parent, false)

        return MyViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(messageList[position])
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}