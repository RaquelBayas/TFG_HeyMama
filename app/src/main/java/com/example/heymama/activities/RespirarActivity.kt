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
import com.example.heymama.models.User
import com.github.florent37.viewanimator.AnimationListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RespirarActivity : AppCompatActivity(), Utils {

    private lateinit var txt_exhalar: TextView
    private lateinit var btn_empezar_respirar: Button
    private lateinit var btn_parar_respiracion: Button
    private lateinit var animation: ViewAnimator
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var rol: String
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respirar)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        txt_exhalar = findViewById(R.id.txt_exhalar)

        btn_empezar_respirar = findViewById(R.id.btn_empezar_respirar)
        btn_empezar_respirar.setOnClickListener {
            start_breathing()
        }
        btn_parar_respiracion = findViewById(R.id.btn_parar_respiracion)
        btn_parar_respiracion.setOnClickListener {
            animation.cancel()
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bottom_item_home -> {
                    finish()
                    when (rol) {
                        "Usuario" -> startActivity(Intent(this, HomeActivity::class.java))
                        "Profesional" -> startActivity(Intent(this, HomeActivityProf::class.java))
                        "Admin" -> startActivity(Intent(this, HomeActivityAdmin::class.java))
                    }
                }
                R.id.nav_bottom_item_foros -> {
                    startActivity(Intent(this, ForosActivity::class.java))
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    /**
     * Obtener el rol del usuario
     *
     */
    private fun getDataUser(){
        database.reference.child("Usuarios").child(auth.uid.toString()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                rol = user!!.rol.toString()
            }
            override fun onCancelled(error: DatabaseError) {
            //TO DO("Not yet implemented")
            }
        })
    }
    /**
     * Este método sirve para empezar la animación de control de la respiración.
     *
     * @param input
     *
     */
    private fun start_breathing() {
       var img_respirar : ImageView = findViewById(R.id.img_respirar)

         animation = ViewAnimator.animate(img_respirar)
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