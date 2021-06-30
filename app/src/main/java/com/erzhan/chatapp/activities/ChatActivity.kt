package com.erzhan.chatapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.adapters.MessageAdapter
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.models.Message
import com.erzhan.chatapp.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var messageEditText: EditText
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter

    private var user: User? = null
    private var chat: Chat? = null
    private lateinit var messageList: ArrayList<Message>
    private val myUserId = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messageEditText = findViewById(R.id.messageEditTextId)
        messageRecyclerView = findViewById(R.id.messageRecyclerViewId)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, this)

        chat = intent.getSerializableExtra("chat") as Chat?
        user = intent.getSerializableExtra("user") as User?

        if (chat == null) {
            val userIds = ArrayList<String>()
            userIds.add(user?.id!!)
            userIds.add(myUserId!!)
            if (!chatId(userIds)) {
                chat = Chat()
                chat!!.userIds = userIds
                title = user!!.name
            }
        } else {
            if (user == null) {
                getMessages()
                initList()
                FirebaseFirestore.getInstance().collection("users")
                    .document(chat!!.userIds[0])
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        user = documentSnapshot.toObject(User::class.java)
                        title = user!!.name
                    }
            }
        }
    }

    private fun chatId(userIds: ArrayList<String>): Boolean {
        var isChat = false
        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("userIds", userIds)
            .get()
            .addOnSuccessListener { snapshots: QuerySnapshot? ->
                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        isChat = true
                        chat = snapshot.toObject(Chat::class.java)
                        chat!!.id = snapshot.id
                        Log.v("Chat Activity :", "chaaat - ${chat!!.id}")
                    }
                }
            }
        return isChat
    }

    private fun initList() {
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(this, messageList, this)
        messageRecyclerView.adapter = messageAdapter
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "clicked $position", Toast.LENGTH_SHORT).show()
    }

    private fun getMessages() {
        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .document(chat?.id!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    for (change: DocumentChange in value.documentChanges) {
                        when (change.type) {
                            DocumentChange.Type.ADDED -> {
                                val message = change.document.toObject(Message::class.java)
                                message.time = change.document.getTimestamp("timestamp")
                                messageList.add(message)
                                break
                            }
                            DocumentChange.Type.MODIFIED -> break
                            DocumentChange.Type.REMOVED -> break
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                }
            }
    }

    fun onClickSend(view: View) {
        val text: String = messageEditText.text.toString().trim()
        if (!TextUtils.isEmpty(text)) {
            if (chat?.id != null) {
                sendMessage(text)
            } else {
                createChat(text)
            }
        }
        messageEditText.text.clear()
    }

    private fun sendMessage(text: String) {
        val map = mapOf<String, Any>(
            "text" to text,
            "senderId" to myUserId!!,
            "timestamp" to Timestamp(Date())
        )

        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .document(chat?.id!!)
            .collection("messages")
            .add(map)
            .addOnSuccessListener {
                initList()
                messageAdapter.notifyDataSetChanged()
                Toast.makeText(this, "SendMessage: $text", Toast.LENGTH_LONG).show()
            }
    }

    private fun createChat(text: String) {
        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .add(chat!!)
            .addOnSuccessListener {
                chat?.id = it.id
                sendMessage(text)
                getMessages()
                initList()
            }
    }
}