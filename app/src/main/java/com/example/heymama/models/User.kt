package com.example.heymama.models

data class User(
    var id: String? = "",
    var name: String? = "",
    var username: String? = "",
    var email: String? = "",
    var rol: String? = "",
    var protected: Boolean? = false,
    var status: String = "offline",
    var bio: String? = "",
    var profilePhoto: String? = ""
)
{
    var _id = id
    get() {
        return _id
    }

    var _name = name
    get() {
        return _name
    }
}