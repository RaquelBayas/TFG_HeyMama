package app.example.heymama.activities

import PreferencesManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import app.example.heymama.R
import app.example.heymama.Utils
import app.example.heymama.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        if(prefs.isLogin()) {
            startActivity(Intent(this, Login::class.java))
            finish()
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