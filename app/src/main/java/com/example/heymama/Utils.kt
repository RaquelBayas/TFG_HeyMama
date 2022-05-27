package com.example.heymama

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getUID(): String {
        return FirebaseAuth.getInstance().uid.toString()
    }

    fun  updateStatus(status: String) {
        val reference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Usuarios").child(getUID())
        val map = HashMap<String, Any>()
        map["status"] = status
        reference.updateChildren(map)
    }

}