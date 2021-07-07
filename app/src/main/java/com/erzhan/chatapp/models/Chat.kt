package com.erzhan.chatapp.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Chat() : Parcelable {
    var id: String? = null
    var userIds: List<String> = ArrayList()

    @ServerTimestamp
    @SerializedName("time")
    var time: Timestamp? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        userIds = parcel.createStringArrayList()!!
        time = parcel.readParcelable(Timestamp::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeStringList(userIds)
        parcel.writeParcelable(time, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat {
            return Chat(parcel)
        }

        override fun newArray(size: Int): Array<Chat?> {
            return arrayOfNulls(size)
        }
    }
}