package app.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Notification(
    val uid: String? = "",
    val nameuser: String? = "",
    val idpost: String? = "",
    val textpost: String? = "",
    val type: String? = "",
    @ServerTimestamp
    var timestamp: Date? = null,
    ): Comparable<Notification> {

    override fun compareTo(other: Notification): Int {
        return other.timestamp?.compareTo(this.timestamp!!)!!
    }
}