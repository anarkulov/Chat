package com.erzhan.chatapp.models

import java.io.Serializable

class Chat : Serializable {
    var id: String? = null
    var userIds: List<String> = ArrayList()
}