package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.heymama.R
import com.example.heymama.interfaces.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView

class RespirarActivity : AppCompatActivity(), Utils {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respirar)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_home -> onClick(R.id.nav_bottom_item_home)
                R.id.nav_bottom_item_respirar -> onClick(R.id.nav_bottom_item_respirar)
            }
        }
    }

    override fun onClick(view: Int) {
        when(view) {
            R.id.nav_bottom_item_home -> finish()
        }
    }

}