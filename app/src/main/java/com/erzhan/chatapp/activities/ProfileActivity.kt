package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.erzhan.chatapp.Constants.Companion.NAME_FIELD
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        title = getString(R.string.profile)

        nameEditText = findViewById(R.id.nameEditTextId)
    }

    fun onClickNext(view: View) {
        val name = nameEditText.text.toString().trim()
        if (TextUtils.isEmpty(name)){
            nameEditText.error = "Enter your name"
            return
        }

        val map = HashMap<String, Any>()
        map[NAME_FIELD] = name
        val myUserId = FirebaseAuth.getInstance().uid
        if (myUserId != null) {
            FirebaseFirestore
                .getInstance()
                .collection(USERS_PATH)
                .document(myUserId)
                .set(map)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Error ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        finish()
    }
}