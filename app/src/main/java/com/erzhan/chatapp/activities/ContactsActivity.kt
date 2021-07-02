package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erzhan.chatapp.Constants.Companion.USER_KEY
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.erzhan.chatapp.adapters.ContactsAdapter
import com.erzhan.chatapp.interfaces.OnItemClickListener
import com.erzhan.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val userList: ArrayList<User> = ArrayList()
    private lateinit var contactsProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        title = getString(R.string.contacts_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        contactsRecyclerView = findViewById(R.id.contactsRecyclerViewId)
        contactsProgressBar = findViewById(R.id.contactsProgressBarId)

        contactsRecyclerView.visibility = GONE

        initList()
        getContactList()
    }

    private fun initList() {
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        contactsAdapter = ContactsAdapter(this, userList, this)
        contactsRecyclerView.adapter = contactsAdapter
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(USER_KEY, userList[position])
        startActivity(intent)
    }

    private fun getContactList() {
        val myUserId = FirebaseAuth.getInstance().uid

        FirebaseFirestore
            .getInstance()
            .collection(USERS_PATH)
            .get()
            .addOnSuccessListener {snapshots ->
            userList.clear()
            for (snapshot: DocumentSnapshot in snapshots){
                val user: User? = snapshot.toObject(User::class.java)
                if (user != null) {
                    if (user.id != myUserId) {
                        user.id = snapshot.id
                        userList.add(user)
                    }
                }
            }
            contactsAdapter.notifyDataSetChanged()
        }
        contactsRecyclerView.visibility = VISIBLE
        contactsProgressBar.visibility = GONE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}