package com.erzhan.chatapp.models

import com.erzhan.chatapp.Constants.Companion.IS_READ_FIELD
import com.erzhan.chatapp.Constants.Companion.TIMESTAMP_FIELD
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

class Message : Serializable {
    var id: String = ""
    var text: String = ""
    var senderId: String = ""
    @PropertyName(IS_READ_FIELD)
    var isRead: Boolean = false

    @ServerTimestamp
    @PropertyName(TIMESTAMP_FIELD)
    var time: Timestamp? = null
}