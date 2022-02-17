package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.heymama.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    object UserInfo {
        var listaMails: MutableList<String> = mutableListOf()

    }

    private lateinit var txt_email: EditText
    private lateinit var txt_password: EditText
    private lateinit var rol: String

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<TextView>(R.id.txt_recordar_contraseña).setOnClickListener {
            startActivity(Intent(this, RememberPassword::class.java))
        }

        txt_email = findViewById(R.id.txt_email2)
        txt_password = findViewById(R.id.txt_password2)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.reference.child("Usuarios")

        findViewById<Button>(R.id.btnAcceder).setOnClickListener{

            dataBaseReference.addValueEventListener(object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    for(dataSnapShot in p0.children) {
                        dataBaseReference.child(dataSnapShot.key.toString()).addValueEventListener(object:ValueEventListener{
                            override fun onCancelled(p1: DatabaseError) {
                            }
                            override fun onDataChange(p1: DataSnapshot) {
                                val data = p1.child("Email").value
                                UserInfo.listaMails.add(data.toString())
                            }
                        })
                    }
                }
            })
            logIn(UserInfo.listaMails)
        }
    }

    private fun logIn(mutableList: MutableList<String>?) {
        val email: String = txt_email.text.toString()
        val password: String = txt_password.text.toString()

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val emailFireBase = auth.currentUser!!
                        val mailVerified = emailFireBase.isEmailVerified
                        dataBaseReference.child(auth.currentUser!!.uid).child("Verified").setValue(mailVerified.toString())
                        auth.currentUser?.reload()
                        if(mailVerified) {
                            checkRol(email,dataBaseReference)
                        } else {
                            Toast.makeText(this, "Debes registrarte primero.", Toast.LENGTH_LONG).show()
                        }
                    }else {
                        if(mutableList!!.contains(email)) {
                            Toast.makeText(this, "La contraseña es incorrecta.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "No existe este email.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
        } else {
            Toast.makeText(this, "Rellena los datos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkRol(email:String, databaseReference: DatabaseReference)  {

        databaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(s in snapshot.children) {
                    if (s.child("Email").value.toString().equals(email)) {
                        rol = s.child("Rol").value.toString()
                        if (rol.equals("Profesional")) {
                            val intent = Intent(applicationContext, HomeActivityProf::class.java)
                            intent.putExtra("Rol","Profesional")
                            startActivity(intent)

                            Log.d("TAG Profesional: ", rol)
                        } else {
                            val intent = Intent(applicationContext, HomeActivity::class.java)
                            intent.putExtra("Rol","Usuario")
                            startActivity(intent)
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
}