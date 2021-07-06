package com.erzhan.chatapp.adapters

import android.app.ActionBar
import android.content.Context
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*


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
        private val linearLayout: LinearLayout = itemView.findViewById(R.id.wholeTextView)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextViewId)
        private val messageTimeTextView : TextView = itemView.findViewById(R.id.messageTimeTextViewId)

        private val markMessage: ImageView = itemView.findViewById(R.id.markImageViewId)

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
            val myUserID = FirebaseAuth.getInstance().uid
            messageTextView.text = message.text
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = Date((message.time?.toDate()!!.time))
            messageTimeTextView.text = format.format(time)
            if (message.senderId == myUserID) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                }
                linearLayout.layoutParams = params
                markMessage.visibility = View.VISIBLE
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