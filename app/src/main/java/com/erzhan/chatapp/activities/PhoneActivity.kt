package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.erzhan.chatapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {

    private lateinit var phoneEditText: EditText
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)
        title = "Phone"

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
        val options: PhoneAuthOptions = PhoneAuthOptions
            .newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        Log.v("Phone Activity", "yes")
        FirebaseAuth
            .getInstance()
            .signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@PhoneActivity,
                        "Auth success",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@PhoneActivity, ProfileActivity::class.java))
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
}