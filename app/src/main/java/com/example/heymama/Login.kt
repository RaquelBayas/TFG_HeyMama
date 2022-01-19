package com.example.heymama

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    object UserInfo {
        var listaMails: MutableList<String> = mutableListOf()

    }

    lateinit var txt_email: EditText
    lateinit var txt_password: EditText

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
                                var data = p1.child("Email").value
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
                            startActivity(Intent(this,HomeActivity::class.java))
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
}