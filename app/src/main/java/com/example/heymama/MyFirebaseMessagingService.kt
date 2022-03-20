package com.example.heymama

import android.app.Notification
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        /*var title : String = p0.notification?.title.toString()
        var body : String = p0.notification?.body.toString()

        var builder : NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,101)
            .setContentTitle("New mail from " + title)
            .setContentText(body)
            .setSmallIcon(R.drawable.iconohome)


        var manager : NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(123,builder)
        */

        Log.e("msg","msg received")
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        Log.d("TOKEN", "Refreshed token: $p0")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.

    }

}