package com.example.heymama.activities

import PreferencesManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    object UserInfo {
        var listaMails: MutableList<String> = mutableListOf()
    }

    private lateinit var txt_email: EditText
    private lateinit var txt_password: EditText
    private lateinit var rol: String
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var prefs: PreferencesManager
    private lateinit var binding : ActivityLoginBinding

    /**
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        if(prefs.isLogin()) {
            var prefs_email = prefs.preferences?.getString("email","")
            var prefs_password = prefs.preferences?.getString("password","")
            var rol = prefs.preferences?.getString("rol","")
            goHomeActivity(rol.toString())
            finish()
        } else {

            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            binding.txtRecordarContraseA.setOnClickListener {
                startActivity(Intent(this, RememberPassword::class.java))
            }

            txt_email = binding.txtEmail2
            txt_password = binding.txtPassword2

            dataBase = FirebaseDatabase.getInstance()
            auth = FirebaseAuth.getInstance()
            dataBaseReference = dataBase.reference.child("Usuarios")

            binding.btnAcceder.setOnClickListener {
                dataBaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }
                    override fun onDataChange(p0: DataSnapshot) {
                        for (dataSnapShot in p0.children) {
                            dataBaseReference.child(dataSnapShot.key.toString())
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p1: DatabaseError) {
                                    }

                                    override fun onDataChange(p1: DataSnapshot) {
                                        val data = p1.child("email").value
                                        UserInfo.listaMails.add(data.toString())
                                    }
                                })
                        }
                    }
                })
                logIn()
            }
        }
    }

    /**
     * Este método permite iniciar sesión en la aplicación
     *
     */
    private fun logIn() {
        val email: String = txt_email.text.toString()
        val password: String = txt_password.text.toString()

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val emailFireBase = auth.currentUser!!
                        val mailVerified = emailFireBase.isEmailVerified

                        auth.currentUser?.reload()
                        if(mailVerified) {
                            checkRol(email,password)
                        } else {
                            checkRegister(emailFireBase,mailVerified)
                            Utils.showToast(this, "Debes registrarte primero.")
                        }
                    } else {
                        Utils.showToast(this, "Comprueba los datos introducidos.")
                    }
                }
        } else {
           Utils.showToast(this, "Rellena los datos.")
        }
    }

    /**
     *
     * @param email FirebaseUser
     * @param mailVerified Boolean
     *
     */
    private fun checkRegister(email: FirebaseUser,mailVerified:Boolean) {
        dataBaseReference.get().addOnSuccessListener { value ->
            if(value.child(email.uid).exists() && !mailVerified) {
                email?.sendEmailVerification().addOnCompleteListener(this) { task ->
                    if (task.isComplete) {
                        task.exception?.printStackTrace()
                        Utils.showToast(this, "Comprueba tu email.")
                    } else {
                        Utils.showToast(this, "Oh, algo ha ido mal.")
                    }
                }
            }
        }
    }

    /**
     * Este método permite comprobar el rol del usuario logueado
     *
     * @param email String
     * @param databaseReference DatabaseReference
     *
     */
    private fun  checkRol(email:String, password:String)  {
        dataBaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(s in snapshot.children) {
                    if (s.child("email").value.toString() == email) {
                        rol = s.child("rol").value.toString()
                        prefs.createLoginSession(email,password,rol)
                        goHomeActivity(rol)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     *
     */
    private fun goHomeActivity(rol: String){
        when (rol) {
            "Profesional" -> {
                finish()
                val intent = Intent(applicationContext, HomeActivityProf::class.java)
                intent.putExtra("Rol","Profesional")
                startActivity(intent)
            }
            "Admin" -> {
                val intent = Intent(applicationContext, HomeActivityAdmin::class.java)
                intent.putExtra("Rol","Admin")
                startActivity(intent)
            }
            else -> {
                val intent = Intent(applicationContext, HomeActivity::class.java)
                intent.putExtra("Rol","Usuario")
                startActivity(intent)
            }
        }
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        Utils.updateStatus("offline")
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        Utils.updateStatus("online")
    }
}