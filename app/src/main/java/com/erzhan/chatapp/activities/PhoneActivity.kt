package com.erzhan.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.erzhan.chatapp.Constants.Companion.PHONE_KEY
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
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)
        title = getString(R.string.phone)

        phoneEditText = findViewById(R.id.phoneEditTextId)
        phoneNumber = phoneEditText.text.toString().trim()

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
        if (!TextUtils.isEmpty(phoneNumber)) {
            val options: PhoneAuthOptions = PhoneAuthOptions
                .newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(30L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } else {
            Toast.makeText(this, "invalid number", Toast.LENGTH_LONG).show()
        }
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        try {
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
                            val intent = Intent(this@PhoneActivity, ProfileActivity::class.java)
                            intent.putExtra(PHONE_KEY, phoneNumber)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@PhoneActivity, MainActivity::class.java)

                            startActivity(intent)
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
        } catch (npe: NullPointerException) {
            npe.printStackTrace()
        }

    }

    private fun hasName(): Boolean {
        var isTrue = false
        val myUserId = FirebaseAuth.getInstance().uid
        if (myUserId != null) {
            try {
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
            } catch (npe: java.lang.NullPointerException) {
                npe.printStackTrace()
            }

        }
        return isTrue
    }
}
