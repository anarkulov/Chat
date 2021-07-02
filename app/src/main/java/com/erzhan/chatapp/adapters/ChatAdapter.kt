package com.erzhan.chatapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.models.Message
import com.erzhan.chatapp.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(
    context: Context,
    chats: List<Chat>,
    onItemCLickListener: OnItemClickListener
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    private val chatList: List<Chat>
    private val inflater: LayoutInflater
    private var onItemClickListener: OnItemClickListener

    init {
        this.chatList = chats
        inflater = LayoutInflater.from(context)
        this.onItemClickListener = onItemCLickListener
    }

    class MyViewHolder(itemView: View, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val chatNameTextView: TextView = itemView.findViewById(R.id.chatNameTextViewId)
        private val lastMessageTextView: TextView =
            itemView.findViewById(R.id.lastMessageTextViewId)
        private val timeTextView: TextView = itemView.findViewById(R.id.lastMessageTimeTextViewId)
        private var onItemClickListener: OnItemClickListener

        init {
            this.onItemClickListener = onItemClickListener
            itemView.setOnClickListener { v: View ->
                onClick(
                    v
                )
            }
        }

        fun bind(chat: Chat) {
            FirebaseFirestore.getInstance().collection("users")
                .document(getN(chat))
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null || user?.name != null) {
                        chatNameTextView.text = user.name
                        setLastMessage(chat.id!!)
                    } else {
                        chatNameTextView.text = chat.id
                    }
                }
        }

        private fun setLastMessage(chatID: String): ArrayList<Message> {
            val messageData = ArrayList<Message>()
            FirebaseFirestore
                .getInstance()
                .collection("chats")
                .document(chatID)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { snapshots ->
                    lastMessageTextView.text = snapshots.last().get("text") as String
                    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val time =
                        Date((snapshots.last().get("timestamp") as Timestamp?)?.toDate()!!.time)
                    timeTextView.text = format.format(time)
                }
                .addOnFailureListener {
                    lastMessageTextView.text = "Message"
                    timeTextView.text = "12:00"
                }

            return messageData
        }

        private fun getN(chat: Chat): String {
            val myid = FirebaseAuth.getInstance().uid
            if (chat.userIds[0] == myid) {
                return chat.userIds[1]
            }
            return chat.userIds[0]
        }

        override fun onClick(v: View) {
            onItemClickListener.onItemClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = inflater.inflate(R.layout.list_chat, parent, false)

        return MyViewHolder(view, onItemClickListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}