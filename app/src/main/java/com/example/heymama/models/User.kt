package com.example.heymama.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class User(
    var id: String? = "",
    var name: String? = "",
    var username: String? = "",
    var email: String? = "",
    var rol: String? = "",
    var protected: Boolean? = false,
    var status: String? = "offline",
    var bio: String? = "",
    var profilePhoto: String? = ""
) : Parcelable
{
    var _id = id
    get() {
        return _id
    }

    var _name = name
    get() {
        return _name
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()) {
    }

    override fun describeContents(): Int {
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
       parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeString(rol)
        parcel.writeBoolean(protected!!)
        parcel.writeString(status)
        parcel.writeString(bio)
        parcel.writeString(profilePhoto)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}