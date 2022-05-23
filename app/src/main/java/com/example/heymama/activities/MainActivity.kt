package com.example.heymama.activities

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
import com.example.heymama.R
import com.example.heymama.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        if (!isNetworkAvailable) {
            AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Conexión a Internet no disponible")
                .setMessage("Comprueba tu conexión a Internet")
                .setPositiveButton("Cerrar") { dialogInterface, i -> finish() }.show()
        } else if (isNetworkAvailable){
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

    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val capabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    if (capabilities != null) {
                        when {
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                                return true
                            }
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                                return true
                            }
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                                return true
                            }
                        }
                    }
                }
            }
            return false
        }

}