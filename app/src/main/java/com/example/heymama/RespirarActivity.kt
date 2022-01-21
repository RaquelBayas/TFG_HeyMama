package com.example.heymama

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RespirarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respirar)

        // Home button
        var btn_home: Button = findViewById(R.id.button)
        btn_home.setOnClickListener {
            onClick(R.id.button)
        }
    }

    fun onClick(view: Int) {
        when(view) {
            R.id.button -> goToActivity(this,HomeActivity::class.java)
            R.id.button2 -> goToActivity(this, ForosActivity::class.java)
            R.id.button3 -> goToActivity(this, PerfilActivity::class.java)
            //R.id.button4 -> goToActivity(this, PerfilActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        activity.finish()
    }
}