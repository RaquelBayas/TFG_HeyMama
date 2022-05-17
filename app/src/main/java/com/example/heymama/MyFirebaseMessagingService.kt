package com.example.heymama

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]
        val sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPreferences.getString("currentUser","none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null && sented==firebaseUser.uid) {
            if(currentOnlineUser != null) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
        /*Looper.prepare()
        Handler().post  {
            Toast.makeText(this,p0.notification?.title,Toast.LENGTH_LONG).show()
        }
        Looper.loop()
        */

        /*var title : String = p0.notification?.title.toString()

        var body : String = p0.notification?.body.toString()

        var builder : NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,101)
            .setContentTitle("New mail from " + title)
            .setContentText(body)
            .setSmallIcon(R.drawable.iconohome)


        var manager : NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(123,builder)
        */

        /*FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            var pushToken = task.result
            Log.i("PUSH_TOKEN", "pushToken: $pushToken")
        }

        Log.e("msg:","msg received")
    */
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification

    }
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

       // val token: String = FirebaseInstanceId.getInstance().getToken()
        Log.d("TOKEN", "Refreshed token: $p0")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.

    }

}