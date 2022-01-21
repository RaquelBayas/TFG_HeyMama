package com.example.heymama

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ForosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foros)

        var txt_depresion : TextView = findViewById(R.id.txt_depresion)
        txt_depresion.setOnClickListener{
            foro_depresion()
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

    private fun foro_depresion() {
        val intent = Intent(this, SubForoActivity::class.java)
        startActivity(intent)
    }
}