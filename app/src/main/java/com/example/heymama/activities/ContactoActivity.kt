package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.heymama.*

class ContactoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        // Home button
        var btn_home: Button = findViewById(R.id.btn_home)
        btn_home.setOnClickListener {
            onClick(R.id.btn_home)
        }

    }

    fun onClick(view: Int) {
        when(view) {
            R.id.btn_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.txt_foros -> goToActivity(this, ForosActivity::class.java)
            R.id.btn_home -> goToActivity(this, HomeActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }
}