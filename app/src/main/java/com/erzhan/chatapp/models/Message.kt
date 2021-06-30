package com.erzhan.chatapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

class Message : Serializable {
    var text: String = ""
    var senderId: String = ""
    @ServerTimestamp
    var time: Timestamp? = null
}