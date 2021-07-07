package com.erzhan.chatapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants
import com.erzhan.chatapp.Constants.Companion.CHAT_KEY
import com.erzhan.chatapp.Constants.Companion.CHATS_PATH
import com.erzhan.chatapp.Constants.Companion.CHAT_TIME_FIELD
import com.erzhan.chatapp.Constants.Companion.NAME_FIELD
import com.erzhan.chatapp.Constants.Companion.NAME_KEY
import com.erzhan.chatapp.Constants.Companion.PHONE_FIELD
import com.erzhan.chatapp.Constants.Companion.PHONE_KEY
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.adapters.ChatAdapter
import com.erzhan.chatapp.fcm.MyFirebaseMessagingService
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList: ArrayList<Chat> = ArrayList()
    private lateinit var chatsProgressBar: ProgressBar
    private lateinit var recipientToken: String

    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PhoneActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = getString(R.string.chats)

        val myUserId = FirebaseAuth.getInstance().currentUser?.uid
        MyFirebaseMessagingService.sharedPreferences =
            getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        FirebaseMessaging.getInstance().subscribeToTopic("topics/$myUserId")
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            MyFirebaseMessagingService.token = it.token
            recipientToken = it.token
        }

        chatRecyclerView = findViewById(R.id.chatRecyclerViewId)
        chatsProgressBar = findViewById(R.id.chatsProgressBarId)
        chatRecyclerView.visibility = GONE
        initList()
        getChats()
    }

    override fun onResume() {
        super.onResume()
        getChats()
    }

    private fun initList() {
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        chatAdapter = ChatAdapter(this, chatList, this)
        chatRecyclerView.adapter = chatAdapter
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(CHAT_KEY, chatList[position])
        startActivity(intent)
    }

    private fun getChats() {
        try {
            FirebaseAuth.getInstance().uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(CHATS_PATH)
                    .orderBy(CHAT_TIME_FIELD, Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        if (snapshots != null) {
                            chatList.clear()
                            for (snapshot: DocumentSnapshot in snapshots) {
                                val chat: Chat? = snapshot.toObject(Chat::class.java)
                                if (chat != null) {
                                    chat.id = snapshot.id
                                    chat.time = snapshot.get(CHAT_TIME_FIELD) as Timestamp
                                    chatList.add(chat)
                                }
                            }
                            chatAdapter.notifyDataSetChanged()
                        }
                    }
            }
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }

        chatsProgressBar.visibility = GONE
        chatRecyclerView.visibility = VISIBLE
    }

    fun onClickContacts(view: View) {
        startActivity(Intent(this, ContactsActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut()
            onStart()
            return true
        } else if (item.itemId == R.id.profile) {
            try {
                FirebaseAuth.getInstance().uid?.let {
                    FirebaseFirestore
                        .getInstance()
                        .collection(USERS_PATH)
                        .document(it)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val name = task.result?.get(NAME_FIELD).toString()
                                Log.v("MainActivity", name)
                                val intent = Intent(this, ProfileActivity::class.java)
                                intent.putExtra(NAME_KEY, name)
                                startActivity(intent)
                            }
                        }
                }
            } catch (npe: NullPointerException) {
                npe.printStackTrace()
            }
        }
        return false
    }
}