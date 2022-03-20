package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Comment(var post: String = "",
              var userID: String = "",
              @ServerTimestamp
              var timestamp: Date? = null) : Comparable<Comment> {

    override fun compareTo(other: Comment): Int {
        return other.timestamp!!.compareTo(this.timestamp)
    }
}