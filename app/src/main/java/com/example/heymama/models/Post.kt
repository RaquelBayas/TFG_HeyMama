package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Post(var title: String = "",
    var post: String = "",
    var userID: String = "",
                @ServerTimestamp
    var timestamp: Date? = null)