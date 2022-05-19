package com.example.heymama.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class PostTimeline(
    val postId: String? = "",
    val userId: String? = "",
    /*val user: User? = null,*/
    @ServerTimestamp
    var timestamp: Date? = null,
    val comment: String? = "",
    var commentCount: Int = 0,
    var likeCount: Int = 0
) : Parcelable, Comparable<PostTimeline> {

    override fun compareTo(other: PostTimeline): Int {
        return other.timestamp?.compareTo(this.timestamp!!)!!
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        /*parcel.readValue(User::class.java.classLoader) as User,*/
        parcel.readValue(Date::class.java.classLoader) as Date,
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(comment)
        parcel.writeInt(commentCount)
        parcel.writeInt(likeCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostTimeline> {
        override fun createFromParcel(parcel: Parcel): PostTimeline {
            return PostTimeline(parcel)
        }

        override fun newArray(size: Int): Array<PostTimeline?> {
            return arrayOfNulls(size)
        }
    }
}