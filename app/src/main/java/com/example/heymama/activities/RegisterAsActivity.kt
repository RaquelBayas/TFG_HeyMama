package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.heymama.R
import com.example.heymama.databinding.ActivityRegisterAsBinding

class RegisterAsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterAsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterAsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUsuario.setOnClickListener {
            initRegister("Usuario")
        }

        binding.btnProfesional.setOnClickListener {
          initRegister("Profesional")
        }
    }

    private fun initRegister(rol: String){
        val intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("Rol",rol)
        startActivity(intent)
    }
}