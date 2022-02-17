package com.example.heymama.models

data class PostTimeline(
                         val userId: String = "",
                         val user: User? = null,
                         val date: String = "",
                         val comment: String = "",
                         var commentCount: Int = 0,
                         var rtCount: Int = 0,
                         var likeCount: Int = 0
)