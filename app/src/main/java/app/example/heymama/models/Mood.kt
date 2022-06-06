package app.example.heymama.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Mood(
    var id: String? = null,
    var mood: String? = null,
    @ServerTimestamp
    var timestamp: Date? = null
) : Parcelable, Comparable<Mood> {

    override fun compareTo(other: Mood): Int {
        return other.timestamp?.compareTo(this.timestamp!!)!!
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as Date) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id!!)
        parcel.writeString(mood)
        parcel.writeValue(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Mood> {
        override fun createFromParcel(parcel: Parcel): Mood {
            return Mood(parcel)
        }

        override fun newArray(size: Int): Array<Mood?> {
            return arrayOfNulls(size)
        }
    }
}