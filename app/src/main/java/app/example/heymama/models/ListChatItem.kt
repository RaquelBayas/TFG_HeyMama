package app.example.heymama.models

import java.util.*

data class ListChatItem(
    var idChat: String = "",
    var idUser: String = "",
    var name: String = "",
    var username: String = "",
    var lastMessage: String = "",
    var status: String,
    var timestamp: Date? = null

) {
}