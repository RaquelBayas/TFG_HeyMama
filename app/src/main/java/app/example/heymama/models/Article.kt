package app.example.heymama.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Article(var idArticle: String? = "",
                    var title: String? = "",
                   var article: String? = "",
                   var professionalID: String? = "",
                   @ServerTimestamp
                   var timestamp: Date? = null): Comparable<Article>, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as Date
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idArticle)
        parcel.writeString(title)
        parcel.writeString(article)
        parcel.writeString(professionalID)
        parcel.writeValue(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Article> {
        override fun createFromParcel(parcel: Parcel): Article {
            return Article(parcel)
        }

        override fun newArray(size: Int): Array<Article?> {
            return arrayOfNulls(size)
        }
    }

    override fun compareTo(other: Article): Int {
        return other.timestamp!!.compareTo(this.timestamp)
    }
}