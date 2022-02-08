package com.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Article(var title: String = "",
                   var article: String = "",
                   var professionalID: String = "",
                   @ServerTimestamp
                   var timestamp: Date? = null)