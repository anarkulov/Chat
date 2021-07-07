package com.erzhan.chatapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.erzhan.chatapp.activities.MainActivity
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val LOG = "MFMS"
    private val channelId = getString(com.erzhan.chatapp.R.string.default_notification_channel_id)

    companion object {
        var sharedPreferences: SharedPreferences? = null

        var token: String?
        get() {
            return sharedPreferences?.getString("token", "")
        }
        set(value) {
            sharedPreferences?.edit()?.putString("token", value)?.apply()
        }
    }



    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.v(LOG, "message: $remoteMessage")

        if (remoteMessage.notification != null){
            sendNotification(remoteMessage.notification?.body, remoteMessage.notification?.title)
        }
    }

    private fun sendNotification(body: String?, title: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(com.erzhan.chatapp.R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ChannelName",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Log.v(LOG, "newToken: $newToken")
        token = newToken
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager){
//        val channelName = "ChannelName"
//        val channel = NotificationChannel(channelId, channelName, IMPORTANCE_HIGH).apply {
//            description = "Description"
//            enableLights(true)
//            lightColor = Color.WHITE
//
//        }
//
//        notificationManager.createNotificationChannel(channel)
//    }
//
//    private fun sendTokenToServer(token: String) {
////        FirebaseMessaging.getInstance()
//    }

}