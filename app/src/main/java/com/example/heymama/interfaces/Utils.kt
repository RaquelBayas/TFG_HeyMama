package com.example.heymama.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent

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