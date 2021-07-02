package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.erzhan.chatapp.Constants.Companion.USERS_PATH
import com.erzhan.chatapp.R
import com.erzhan.chatapp.models.User
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)
        title = getString(R.string.phone)

        phoneEditText = findViewById(R.id.phoneEditTextId)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.v("Phone Activity:", "onVerificationCompleted")
                signIn(phoneAuthCredential)
            }

            override fun onVerificationFailed(firebaseException: FirebaseException) {
                Log.v("Phone Activity:", "onVerificationFailed")
            }

        }
    }

    fun onClickStart(view: View) {
        val phone: String = phoneEditText.text.toString()
        if (!TextUtils.isEmpty(phone)) {
            val options: PhoneAuthOptions = PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phone)
                .setTimeout(10L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this, "invalid number", Toast.LENGTH_LONG).show()
        }
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        FirebaseAuth
            .getInstance()
            .firebaseAuthSettings
            .setAppVerificationDisabledForTesting(true)
        FirebaseAuth.getInstance()
            .signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@PhoneActivity,
                        "Auth success",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (!hasName()) {
                        startActivity(Intent(this@PhoneActivity, ProfileActivity::class.java))
                    } else {
                        startActivity(Intent(this@PhoneActivity, MainActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(
                        this@PhoneActivity,
                        "Auth error ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun hasName(): Boolean {
        var isTrue = false
        val myUserId = FirebaseAuth.getInstance().uid
        if (myUserId != null) {
            FirebaseFirestore
                .getInstance()
                .collection(USERS_PATH)
                .document(myUserId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val user: User? = snapshot.toObject(User::class.java)
                    if (user != null) {
                        user.id = snapshot.id
                        if (!TextUtils.isEmpty(user.name)) {
                            isTrue = true
                        }
                    }
                }
        }
        return isTrue
    }
}
