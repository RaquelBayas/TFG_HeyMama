package com.example.heymama

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast

object Utils {

    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}