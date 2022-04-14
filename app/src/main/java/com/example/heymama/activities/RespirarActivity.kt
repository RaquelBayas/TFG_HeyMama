package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.florent37.viewanimator.ViewAnimator;
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.heymama.R
import com.example.heymama.interfaces.Utils
import com.github.florent37.viewanimator.AnimationListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class RespirarActivity : AppCompatActivity(), Utils {

    private lateinit var txt_exhalar: TextView
    private lateinit var btn_empezar_respirar: Button
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respirar)

        txt_exhalar = findViewById(R.id.txt_exhalar)

        btn_empezar_respirar = findViewById(R.id.btn_empezar_respirar)
        btn_empezar_respirar.setOnClickListener {
            start_breathing()
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_home -> {
                    finish()
                    goToActivity(this,HomeActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_foros -> {goToActivity(this,ForosActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_ajustes -> {
                    goToActivity(this,SettingsActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    /**
     * Este método sirve para empezar la animación de control de la respiración.
     *
     * @param input
     *
     */
    private fun start_breathing() {
       var img_respirar : ImageView = findViewById(R.id.img_respirar)
        ViewAnimator.animate(img_respirar)
            .alpha(0f, 1f)
            .onStart(object: AnimationListener.Start {
                override fun onStart() {
                    txt_exhalar.text = "Inhala... Exhala"
                }

            })
            .scale(0.02f, 1.5f, 0.02f)
            .rotation(360f)
            .repeatCount(5)
            .duration(6500) // 6.5 segundos
            .onStop(object: AnimationListener.Stop {
                override fun onStop() {
                    txt_exhalar.text = ""
                    img_respirar.scaleX = 1.0f
                    img_respirar.scaleY = 1.0f
                }

            })
            .start()
    }

}