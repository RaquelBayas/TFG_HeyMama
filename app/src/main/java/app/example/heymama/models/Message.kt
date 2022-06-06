package app.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp

data class Message(
    var messageID: String = "",
    var senderUID: String = "",
    var receiverUID: String = "",
    var message: String = "",
    var imageUrl: String = "",
    @ServerTimestamp
    var timestamp: Long? = null): Comparable<Message>  {

    override fun compareTo(other: Message): Int {
        return other.timestamp?.compareTo(this.timestamp!!)!!
    }

}