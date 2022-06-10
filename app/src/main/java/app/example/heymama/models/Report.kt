package app.example.heymama.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Report(
                  var postId: String = "",
                  var userId: String = "",
                  @ServerTimestamp
                  var timestamp: Date? = null) : Comparable<Report> {

    override fun compareTo(other: Report): Int {
        return other.timestamp!!.compareTo(this.timestamp)
    }
}