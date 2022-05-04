package com.example.heymama.models

data class User(
    var id: String? = "",
    var name: String? = "",
    var username: String? = "",
    var email: String? = "",
    var rol: String? = "",
    var status: String = "offline",
    var bio: String? = "",
    var profilePhoto: String? = ""
)
{

}