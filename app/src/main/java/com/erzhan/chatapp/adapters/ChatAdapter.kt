package com.erzhan.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

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
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessageTextViewId)
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
            val myid = FirebaseAuth.getInstance().uid
            FirebaseFirestore.getInstance().collection("users")
                .document(getN(chat))
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    if (user != null){
                        chatNameTextView.text = user.name
                        lastMessageTextView.text = myid
                    } else {
                        chatNameTextView.text = chat.id
                    }
                }
        }

        private fun getN(chat: Chat): String {
            val myid = FirebaseAuth.getInstance().uid
            var i = 0x
            while (myid == chat.userIds[i]){
                i++
            }
            return chat.userIds[i]
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