package com.example.heymama.models

import java.util.*

data class ListChatItem(
    var idChat: String = "",
    var name: String = "",
    var username: String = "",
    var lastMessage: String = "",
    var timestamp: Date? = null

) {
}