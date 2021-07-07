package com.erzhan.chatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants.Companion.CHATS_PATH
import com.erzhan.chatapp.Constants.Companion.CHAT_TIME
import com.erzhan.chatapp.Constants.Companion.IS_READ_FIELD
import com.erzhan.chatapp.Constants.Companion.MESSAGES_PATH
import com.erzhan.chatapp.Constants.Companion.SENDER_ID_FIELD
import com.erzhan.chatapp.Constants.Companion.TEXT_FIELD
import com.erzhan.chatapp.Constants.Companion.TIMESTAMP_FIELD
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.models.Message
import com.erzhan.chatapp.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(
    val context: Context,
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
        private val unreadMessagesTextView: TextView =
            itemView.findViewById(R.id.unreadMessagesCountId)
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
            try {
                FirebaseFirestore.getInstance().collection("users")
                    .document(getUserId(chat))
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        val user = documentSnapshot.toObject(User::class.java)
                        if (user != null || user?.name != null) {
                            chatNameTextView.text = user.name
                            setLastMessage(chat)
                            setUnreadMessages(chat)
                        } else {
                            chatNameTextView.text = chat.id
                        }
                    }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }

        }

        private fun setUnreadMessages(chat: Chat) {
            var count = 0
            try {
                FirebaseFirestore
                    .getInstance()
                    .collection(CHATS_PATH)
                    .document(chat.id!!)
                    .collection(MESSAGES_PATH)
                    .whereEqualTo(SENDER_ID_FIELD, getUserId(chat))
                    .whereEqualTo(IS_READ_FIELD, false)
                    .get()
                    .addOnSuccessListener {
                        for (snapshot in it) {
                            count++;
                        }
                        if (count > 0) {
                            unreadMessagesTextView.text = count.toString()
                            unreadMessagesTextView.visibility = VISIBLE
                            lastMessageTextView.setTextColor(Color.WHITE)
                        } else {
                            unreadMessagesTextView.visibility = GONE
                            lastMessageTextView.setTextColor(Color.GRAY)
                        }
                    }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }

        }

        private fun setLastMessage(chat: Chat): ArrayList<Message> {
            val messageData = ArrayList<Message>()
            try {
                FirebaseFirestore
                    .getInstance()
                    .collection(CHATS_PATH)
                    .document(chat.id!!)
                    .collection(MESSAGES_PATH)
                    .orderBy(TIMESTAMP_FIELD, Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        if (snapshots != null) {
                            lastMessageTextView.text = snapshots.last().get(TEXT_FIELD) as String
                            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val time = snapshots.last().get(TIMESTAMP_FIELD) as Timestamp?
                            timeTextView.text = format.format(Date(time?.toDate()!!.time))
                            setChatTime(chat, time)
                        }
                    }
                    .addOnFailureListener {
                        lastMessageTextView.text = "Message"
                        timeTextView.text = "12:00"
                    }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }

            return messageData
        }

        private fun setChatTime(chat: Chat, time: Timestamp) {
            try {
                FirebaseFirestore
                    .getInstance()
                    .collection(CHATS_PATH)
                    .document(chat.id!!)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.reference?.update(CHAT_TIME, time)
                        }
                    }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }
        }

        private fun getUserId(chat: Chat): String {
            val myUserId = FirebaseAuth.getInstance().uid
            if (chat.userIds[0] == myUserId) {
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