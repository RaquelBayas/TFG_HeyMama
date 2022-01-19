package com.example.heymama

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class RegisterAsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_as)

        val btnUsuario: Button = findViewById(R.id.btn_usuario)
        btnUsuario.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("Usuario","Usuario")

            startActivity(intent)
        }

        val btnProfesional: Button = findViewById(R.id.btn_profesional)
        btnProfesional.setOnClickListener {
            // Do something in response to button click
            val intent = Intent(this, RegisterProfesionalActivity::class.java)
            intent.putExtra("Profesional","Profesional")
            startActivity(intent)
        }
    }
}