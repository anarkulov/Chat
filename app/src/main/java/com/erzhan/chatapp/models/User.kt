package com.erzhan.chatapp.models

import android.text.TextUtils
import java.io.Serializable

class User : Serializable {

    var id: String = ""
        set(value) {
            if (!TextUtils.isEmpty(value)){
                field = value
            }
        }

    var name: String = ""
        set(value) {
            if (!TextUtils.isEmpty(value)){
                field = value
            }
        }

}
