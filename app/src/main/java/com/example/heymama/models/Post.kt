package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(
    var id: String = "",
    var title: String = "",
    var post: String = "",
    var userID: String = "",
    var protected: String = "",
                @ServerTimestamp
    var timestamp: Date? = null) : Comparable<Post> {

    override fun compareTo(other: Post): Int {
        return other.timestamp!!.compareTo(this.timestamp)
    }
}