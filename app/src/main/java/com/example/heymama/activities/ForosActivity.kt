package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.heymama.R

class ForosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foros)

        var txt_depresion : TextView = findViewById(R.id.txt_depresion)
        txt_depresion.setOnClickListener{
            onClick(R.id.txt_depresion)
        }

        /*
        var txt_embarazo : TextView = findViewById(R.id.txt_embarazo)
        txt_embarazo.setOnClickListener{
            foro_embarazo()
        }

        var txt_posparto : TextView = findViewById(R.id.txt_posparto)
        txt_posparto.setOnClickListener{
            foro_posparto()
        } */

    }


    fun onClick(view: Int) {
        when(view) {
            R.id.txt_depresion -> goToActivity(this, SubForoActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }
}