package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.sql.Timestamp
import java.util.*

data class Message(var senderUID: String = "",
                   var receiverUID: String = "",
                   var message: String = "",
                   @ServerTimestamp
                   var timestamp: Date? = null)  {
}