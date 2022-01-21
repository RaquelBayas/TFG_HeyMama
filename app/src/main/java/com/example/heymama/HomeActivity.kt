package com.example.heymama

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class HomeActivity : AppCompatActivity() {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var rol: String
    private lateinit var textView: TextView
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        val user: FirebaseUser? = auth.currentUser
        // ID en la BBDD
        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

        user?.let {
            for(profile in it.providerData) {
                val providerId = profile.providerId
                val uid = profile.uid
                email = profile.email.toString()
                //val name = profile.displayName

            }
        }

        checkRol(email,dataBaseReference)
        textView = findViewById(R.id.textView)
        textView.text = email

        var btn_respirar : Button = findViewById(R.id.btn_respirar)
        btn_respirar.setOnClickListener{
            onClick(R.id.btn_respirar)
        }

        var txt_foros : TextView = findViewById(R.id.txt_foros)
        txt_foros.setOnClickListener{
            onClick(R.id.txt_foros)
        }

        var btn_icon_profile : TextView = findViewById(R.id.btn_icon_profile)
        btn_icon_profile.setOnClickListener{
            onClick(R.id.btn_icon_profile)
        }


    }

    private fun checkRol(email:String, databaseReference: DatabaseReference)  {

        databaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(s in snapshot.children) {
                    if (s.child("Email").value.toString().equals(email)) {
                        rol = s.child("Rol").value.toString()
                        if (rol.equals("Profesional")) {
                            Log.d("TAG Profesional: ", rol)
                        } else {
                            Log.d("TAG Usuario: ", rol)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun onClick(view: Int) {
        when(view) {
            R.id.btn_respirar -> goToActivity(this,RespirarActivity::class.java)
            R.id.txt_foros -> goToActivity(this, ForosActivity::class.java)
            R.id.btn_icon_profile -> goToActivity(this, PerfilActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }


}