package com.example.newsapp.pushNotification

import android.util.Log
import com.example.newsapp.utils.utils.TAG
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, " ${message.notification?.title +" , " +message.notification?.body}")
    }
}