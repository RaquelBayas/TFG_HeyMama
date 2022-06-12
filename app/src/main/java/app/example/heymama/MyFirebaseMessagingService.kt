package app.example.heymama

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.media.RingtoneManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import app.example.heymama.activities.ChatActivity


class MyFirebaseMessagingService: FirebaseMessagingService() {

    /**
     * @param remoteMessage RemoteMessage
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]
        val sharedPreferences = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPreferences.getString("currentUser","none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null && sented==firebaseUser.uid) {
            if(currentOnlineUser != user) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    /**
     *
     * @param remoteMessage RemoteMessage
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("friendUID", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder : NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)

        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if (j > 0) {
            i = j
        }

        noti.notify(i, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, ChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("friendUID", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)
        val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon)

        var i = 0
        if (j > 0) {
            i = j
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("TOKEN", "Refreshed token: $p0")
    }
}