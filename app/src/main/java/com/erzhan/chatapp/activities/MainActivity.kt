package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants.Companion.CHAT_KEY
import com.erzhan.chatapp.Constants.Companion.CHATS_PATH
import com.erzhan.chatapp.Constants.Companion.USERS_IDS_FIELD
import com.erzhan.chatapp.R
import com.erzhan.chatapp.models.Chat
import com.erzhan.chatapp.adapters.ChatAdapter
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList: ArrayList<Chat> = ArrayList()
    private lateinit var chatsProgressBar: ProgressBar

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
        chatRecyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        chatAdapter = ChatAdapter(this, chatList, this)
        chatRecyclerView.adapter = chatAdapter
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(CHAT_KEY, chatList[position])
        startActivity(intent)
    }

    private fun getChats() {
        FirebaseFirestore
            .getInstance()
            .collection(CHATS_PATH)
            .whereArrayContains(USERS_IDS_FIELD, FirebaseAuth.getInstance().uid!!)
            .get()
            .addOnSuccessListener { snapshots ->
                chatList.clear()
                for (snapshot: DocumentSnapshot in snapshots) {
                    val chat: Chat? = snapshot.toObject(Chat::class.java)
                    if (chat != null) {
                        chat.id = snapshot.id
                        chatList.add(chat)
                    }
                }
                chatAdapter.notifyDataSetChanged()
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
        }
        return false
    }
}