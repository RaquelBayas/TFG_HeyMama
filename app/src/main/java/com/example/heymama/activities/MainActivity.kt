package com.example.heymama.activities

import PreferencesManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.heymama.R
import com.example.heymama.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        if(prefs.isLogin()) {
            startActivity(Intent(this, Login::class.java))
        } else {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.btnRegister.setOnClickListener {
                startActivity(Intent(this, RegisterAsActivity::class.java))
            }

            binding.btnLogin.setOnClickListener {
                finish()
                startActivity(Intent(this, Login::class.java))
            }
        }
    }

}