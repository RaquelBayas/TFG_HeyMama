package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivityHomeAdminBinding
import com.example.heymama.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeActivityAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityHomeAdminBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.txtConsultas.setOnClickListener{
            startActivity(Intent(this,ListaUsuariosActivity::class.java))
        }


        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_foros ->
                    startActivity(Intent(this,ForosActivity::class.java))

                R.id.nav_bottom_item_ajustes ->
                    startActivity(Intent(this,SettingsActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener false
        }


    }





}