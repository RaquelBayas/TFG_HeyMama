package com.example.heymama.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Article(var title: String? = "",
                   var article: String? = "",
                   var professionalID: String? = "",
                   @ServerTimestamp
                   var timestamp: Date? = null): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as Date
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
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
}