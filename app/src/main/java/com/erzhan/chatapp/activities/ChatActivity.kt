package com.erzhan.chatapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants.Companion.CHAT_KEY
import com.erzhan.chatapp.Constants.Companion.CHATS_PATH
import com.erzhan.chatapp.Constants.Companion.CHAT_TIME_FIELD
import com.erzhan.chatapp.Constants.Companion.IS_READ_FIELD
import com.erzhan.chatapp.Constants.Companion.MESSAGES_PATH
import com.erzhan.chatapp.Constants.Companion.SENDER_ID_FIELD
import com.erzhan.chatapp.Constants.Companion.TEXT_FIELD
import com.erzhan.chatapp.Constants.Companion.TIMESTAMP_FIELD
import com.erzhan.chatapp.Constants.Companion.USERS_IDS_FIELD
import com.erzhan.chatapp.Constants.Companion.USER_KEY
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.erzhan.chatapp.adapters.MessageAdapter
import com.erzhan.chatapp.interfaces.NotificationAPI
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : AppCompatActivity(), OnItemClickListener {

    private var notify: Boolean = false
    private lateinit var messageEditText: EditText
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageProgressBar: ProgressBar

    private var user: User? = null
    private var chat: Chat? = null
    private lateinit var messageList: ArrayList<Message>
    private lateinit var recipientToken: String
    private lateinit var notificationAPI: NotificationAPI
    private val tag = "ChatActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
//
//        MyFirebaseMessagingService.sharedPreferences =
//            getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
//        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
//            MyFirebaseMessagingService.token = it.token
//            recipientToken = it.token
//        }

        notificationAPI = RetrofitInstance.api

        messageEditText = findViewById(R.id.messageEditTextId)
        messageRecyclerView = findViewById(R.id.messageRecyclerViewId)

        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, this)

        messageProgressBar = findViewById(R.id.messageProgressBarId)

        chat = intent.getParcelableExtra(CHAT_KEY) as Chat?
        user = intent.getSerializableExtra(USER_KEY) as User?
        val myUserId = FirebaseAuth.getInstance().currentUser!!.uid

        if (chat == null) {
            val userIds = ArrayList<String>()
            chat = Chat()
            userIds.add(user!!.id)
            userIds.add(myUserId)
            chat?.userIds = userIds
            initList()
            setChat(userIds)
            title = user!!.name
        } else {
            initList()
            if (user == null) {
                try {
                    FirebaseFirestore
                        .getInstance()
                        .collection(USERS_PATH)
                        .document(getUserId(chat!!))
                        .get()
                        .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                            user = documentSnapshot.toObject(User::class.java)
                            title = if (user?.name == null) {
                                chat?.id
                            } else {
                                user!!.name
                            }
                            getMessages()
                        }
                } catch (npe: NullPointerException) {
                    npe.printStackTrace()
                }

            }
        }
    }

    private fun getUserId(chat: Chat): String {
        val myUserId = FirebaseAuth.getInstance().uid
        if (chat.userIds[0] == myUserId) {
            return chat.userIds[1]
        }
        return chat.userIds[0]
    }

    private fun setChat(userIds: ArrayList<String>) {
        try {
            FirebaseFirestore
                .getInstance()
                .collection(CHATS_PATH)
                .whereEqualTo(USERS_IDS_FIELD, userIds)
                .get()
                .addOnSuccessListener { snapshots ->
                    for (snapshot: DocumentSnapshot in snapshots) {
                        chat?.id = snapshot.id
                        chat?.userIds = userIds
                        getMessages()
//                        Log.d(tag, "restore Chat() userids: $userIds")
                    }
                }
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }

        messageProgressBar.visibility = GONE
    }

    private fun initList() {
        val manager = LinearLayoutManager(this);
        manager.reverseLayout = true
        messageRecyclerView.layoutManager = manager
        messageAdapter = MessageAdapter(this, messageList, this)
        messageRecyclerView.adapter = messageAdapter
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this, "clicked $position", Toast.LENGTH_SHORT).show()
    }

    private fun getMessages() {
        setRead()
        try {
            FirebaseFirestore
                .getInstance()
                .collection(CHATS_PATH)
                .document(chat!!.id!!)
                .collection(MESSAGES_PATH)
                .orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshots ->
                    messageList.clear()
                    if (snapshots != null) {
                        for (change: DocumentSnapshot in snapshots) {
                            val message = change.toObject(Message::class.java)
                            if (message != null) {
                                message.text = change.get(TEXT_FIELD) as String
                                message.senderId = change.get(SENDER_ID_FIELD) as String
                                message.isRead = change.get(IS_READ_FIELD) as Boolean
                                message.time = change.getTimestamp(TIMESTAMP_FIELD)
                                messageList.add(message)
                            }
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                }
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }

        messageProgressBar.visibility = GONE
    }

    private fun setRead() {
        try {
            FirebaseFirestore
                .getInstance()
                .collection(CHATS_PATH)
                .document(chat!!.id!!)
                .collection(MESSAGES_PATH)
                .whereEqualTo(SENDER_ID_FIELD, getUserId(chat!!))
                .whereEqualTo(IS_READ_FIELD, false)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!) {
                            document.reference.update(IS_READ_FIELD, true)
                        }
                    }
                }
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }
    }

    fun onClickSend(view: View) {
        notify = true
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
            IS_READ_FIELD to false,
            TIMESTAMP_FIELD to Timestamp(Date())
        )

        try {
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
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }


        recipientToken = "/topics/${user?.id}"
        try {
            PushNotification(
                NotificationData(user!!.name, text), recipientToken
            ).also {
                if (notify) {
                    sendNotification(it)
                }
                notify = false
            }
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }

    }

    private fun createChat(text: String) {
        val map = mapOf<String, Any>(
            USERS_IDS_FIELD to chat!!.userIds,
            CHAT_TIME_FIELD to Timestamp(Date())
        )

        try {
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
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = notificationAPI.postNotification(notification)
                if (response.isSuccessful) {
                    Log.d(tag, "successful response : $response")
                } else {
                    Log.d(tag, "response : ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {
                Log.d(tag, "exception : $e")
            }
        }
}