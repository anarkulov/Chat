package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.R
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.adapters.ChatAdapter
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList: ArrayList<Chat> = ArrayList()

//    companion object {
//        val myUserId: String? = FirebaseAuth.getInstance().uid
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Chats"

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, PhoneActivity::class.java))
            return
        }
        chatRecyclerView = findViewById(R.id.chatRecyclerViewId)

        initList()
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
        intent.putExtra("chat", chatList[position])
        startActivity(intent)
    }

    private fun getChats() {
        val myUserId = FirebaseAuth.getInstance().uid
        if (myUserId != null) {
            FirebaseFirestore
                .getInstance()
                .collection("chats")
                .whereArrayContains("userIds", myUserId)
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        for (change: DocumentChange in value.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    val chat = change.document.toObject(Chat::class.java)
                                    chat.id = change.document.id
                                    chatList.add(chat)
                                    break
                                }
                                DocumentChange.Type.MODIFIED -> break
                                DocumentChange.Type.REMOVED -> break
                            }
                        }
                        chatAdapter.notifyDataSetChanged()
                    }
                }
        }
    }

    fun onClickContacts(view: View) {
        startActivity(Intent(this, ContactsActivity::class.java))
    }
}