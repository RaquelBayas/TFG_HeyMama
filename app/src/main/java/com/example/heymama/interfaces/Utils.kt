package com.example.heymama.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.heymama.R
import com.example.heymama.activities.*

interface Utils {

    fun onClick(view: Int) {
        when(view) {

        }
    }


    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }
}