package com.erzhan.chatapp.activities

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants.Companion.CHAT_KEY
import com.erzhan.chatapp.Constants.Companion.CHATS_PATH
import com.erzhan.chatapp.Constants.Companion.MESSAGES_PATH
import com.erzhan.chatapp.Constants.Companion.SENDER_ID_FIELD
import com.erzhan.chatapp.Constants.Companion.TEXT_FIELD
import com.erzhan.chatapp.Constants.Companion.TIMESTAMP_FIELD
import com.erzhan.chatapp.Constants.Companion.TOPIC
import com.erzhan.chatapp.Constants.Companion.USERS_IDS_FIELD
import com.erzhan.chatapp.Constants.Companion.USER_KEY
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.erzhan.chatapp.adapters.MessageAdapter
import com.erzhan.chatapp.fcm.MyFirebaseMessagingService
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.models.Message
import com.erzhan.chatapp.models.User
import com.erzhan.chatapp.notification.NotificationData
import com.erzhan.chatapp.notification.PushNotification
import com.erzhan.chatapp.notification.RetrofitInstance
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var messageEditText: EditText
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageProgressBar: ProgressBar

    private var user: User? = null
    private var chat: Chat? = null
    private lateinit var messageList: ArrayList<Message>
    private lateinit var recipientToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        MyFirebaseMessagingService.sharedPreferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            MyFirebaseMessagingService.token = it.token
            recipientToken = it.token
        }

        messageEditText = findViewById(R.id.messageEditTextId)
        messageRecyclerView = findViewById(R.id.messageRecyclerViewId)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, this)

        messageProgressBar = findViewById(R.id.messageProgressBarId)

        chat = intent.getSerializableExtra(CHAT_KEY) as Chat?
        user = intent.getSerializableExtra(USER_KEY) as User?
        val myUserId = FirebaseAuth.getInstance().currentUser!!.uid

        if (chat == null) {
            val userIds = ArrayList<String>()
            userIds.add(user!!.id)
            userIds.add(myUserId)
            chat = Chat()
            setChat(userIds)
            title = user!!.name
        } else {
            if (user == null) {
                initList()
                getMessages()
                FirebaseFirestore.getInstance().collection(USERS_PATH)
                    .document(chat!!.userIds[0])
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        user = documentSnapshot.toObject(User::class.java)
                        title = user!!.name
                    }
            }
        }
    }

    private fun setChat(userIds: ArrayList<String>) {
        FirebaseFirestore
            .getInstance()
            .collection(CHATS_PATH)
            .whereEqualTo(USERS_IDS_FIELD, userIds)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot: DocumentSnapshot in snapshots) {
                    chat?.id = snapshot.id
                    initList()
                    getMessages()
                    Log.v("Chat Activity", "restore Chat()")
                }
            }
            .addOnFailureListener {
                chat!!.userIds = userIds
                Log.v("Chat Activity", "new Chat()")
            }
    }

    private fun initList() {
        val manager = LinearLayoutManager(this);
        messageRecyclerView.layoutManager = manager
        messageAdapter = MessageAdapter(this, messageList, this)
        messageRecyclerView.adapter = messageAdapter
        messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "clicked $position", Toast.LENGTH_SHORT).show()
    }

    private fun getMessages() {
        FirebaseFirestore
            .getInstance()
            .collection(CHATS_PATH)
            .document(chat!!.id!!)
            .collection(MESSAGES_PATH)
            .orderBy(TIMESTAMP_FIELD, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                messageList.clear()
                if (snapshots != null) {
                    for (change: DocumentSnapshot in snapshots) {
                        val message = change.toObject(Message::class.java)
                        if (message != null) {
                            message.text = change.get(TEXT_FIELD) as String
                            message.senderId = change.get(SENDER_ID_FIELD) as String
                            message.time = change.getTimestamp(TIMESTAMP_FIELD)
                            messageList.add(message)

                        }
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }
        messageProgressBar.visibility = GONE
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
        val myUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val map = mapOf<String, Any>(
            TEXT_FIELD to text,
            SENDER_ID_FIELD to myUserId,
            TIMESTAMP_FIELD to Timestamp(Date())
        )

        FirebaseFirestore
            .getInstance()
            .collection(CHATS_PATH)
            .document(chat!!.id!!)
            .collection(MESSAGES_PATH)
            .add(map)
            .addOnSuccessListener {
                initList()
                getMessages()
                messageAdapter.notifyDataSetChanged()
            }
        PushNotification(
            NotificationData("Erzhan", text), recipientToken)
            .also {
                sendNotification(it)
        }
    }

    private fun createChat(text: String) {
        val map = mapOf<String, Any>(
            USERS_IDS_FIELD to chat!!.userIds
        )
        FirebaseFirestore
            .getInstance()
            .collection(CHATS_PATH)
            .add(map)
            .addOnSuccessListener {
                chat?.id = it.id
                sendMessage(text)
                initList()
                getMessages()
            }
    }

    val tag = "ChatActivity"
    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.v(tag, "response : $response")
                } else {
                    Log.v(tag, "response : ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {
                Log.v(tag, "exception : ${e.toString()}")

            }
        }
}