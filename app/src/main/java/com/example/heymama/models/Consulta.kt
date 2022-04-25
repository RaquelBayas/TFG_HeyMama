package com.example.heymama.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Consulta(
    var id: String? = null,
    var userID: String? = null, //USER
    var tema: String? = null,
    var consulta: String? = null,
    @ServerTimestamp
    var timestamp: Date? = null): Parcelable,  Comparable<Consulta> {


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        //parcel.readValue(User::class.java.classLoader) as User,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Date::class.java.classLoader) as Date) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userID)
        parcel.writeString(tema)
        parcel.writeString(consulta)
        parcel.writeValue(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Consulta> {
        override fun createFromParcel(parcel: Parcel): Consulta {
            return Consulta(parcel)
        }

        override fun newArray(size: Int): Array<Consulta?> {
            return arrayOfNulls(size)
        }
    }

    // Método sort
    override fun compareTo(other: Consulta): Int {
        return other.timestamp?.compareTo(this.timestamp!!)!!
    }
}