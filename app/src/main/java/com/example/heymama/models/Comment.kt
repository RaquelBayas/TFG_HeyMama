package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Comment(var post: String = "",
              var userID: String = "",
              @ServerTimestamp
              var timestamp: Date? = null)