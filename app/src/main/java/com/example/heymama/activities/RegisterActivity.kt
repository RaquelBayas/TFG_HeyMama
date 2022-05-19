package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.example.heymama.R
import com.example.heymama.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class RegisterActivity : AppCompatActivity() {

    private lateinit var rol: String

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras
        rol = intent.getStringExtra("Rol").toString()

        var actionBar: ActionBar? = supportActionBar
        actionBar?.setTitle("Create account")

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        focusUsername()
        focusEmail()
        focusPassword()
        focusName()

        binding.btnCheckUsername.setOnClickListener {
            validUsername()
        }
        binding.btnCrearCuenta.setOnClickListener {
            validate()
        }

    }

    private fun validate() {
        Toast.makeText(this,"VALIDATE",Toast.LENGTH_SHORT).show()
        val validUser = binding.userLayout.helperText == null
        val validName = binding.nameLayout.helperText == null
        val validEmail = binding.email0Layout.helperText == null
        val validPassword = binding.passwordLayout.helperText == null

        if(validUser && validName && validEmail && validPassword) {
            createAccount()
            Toast.makeText(this,"CREATE",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"Rellena los datos.",Toast.LENGTH_SHORT).show()
           AlertDialog.Builder(this)
               .setTitle("Error")
               .setMessage("Rellena correctamente los datos")
               .setPositiveButton("Okay"){ _,_ ->

               }
        }
    }

    private fun focusUsername() {
        binding.txtUser.setOnFocusChangeListener { view, focused ->
            if(!focused) {
                validUsername()
            }
        }
    }

    private fun validUsername() {
        var username = binding.txtUser.text.toString()
        var userRef = dataBase.getReference("Usernames")

        userRef.get().addOnCompleteListener(this) { task ->
            userRef.get().addOnSuccessListener { value ->

                Log.i("validuser",value.children.toString())
                if (!value.child(username.lowercase()).exists()) {
                    //userRef.child(username).setValue(email)

                    //createAccount()
                    binding.userLayout.helperText = ""
                } else{
                    binding.userLayout.helperText = "Usuario no disponible"
                }
            }.addOnFailureListener {
                binding.userLayout.helperText = "Usuario no disponible"
                Toast.makeText(this, "Username no disponible", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { Toast.makeText(this, "Username no disponible", Toast.LENGTH_SHORT).show() }

    }

    private fun focusName() {
        binding.txtName.setOnFocusChangeListener { view, focused ->
            if(!focused) {
                binding.nameLayout.helperText = validName()
            }
        }
    }

    private fun validName(): String? {
        var name = binding.txtName.text.toString()
        if(name.isEmpty()){
            return "Nombre inválido"
        }
        return null
    }

    private fun focusPassword() {
        binding.txtPassword.doOnTextChanged { text, start, before, count ->
            var password = binding.txtPassword.text.toString()
            if(password == null || password.length < 7){
                binding.passwordLayout.helperText = "Contraseña inválida"
            }else{
                binding.passwordLayout.helperText = null
            }
        }
    }

    private fun validPassword(): String? {
        var password = binding.txtPassword.text.toString()
        if(password == null || password.length < 7){
            return "Contraseña inválida"
        }
        return null
    }

    private fun focusEmail() {
        binding.txt0Email.setOnFocusChangeListener { view, focused ->
            if(!focused) {
                binding.email0Layout.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        var email = binding.txt0Email.text.toString()
        /*if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Correo electrónico inválido"
        }*/
        if(email.isEmpty()) {
            return "Escribe un correo electrónico"
        }
        return null
    }

    private fun createAccount() {
        val email: String = binding.txt0Email.text.toString()
        val name: String = binding.txtName.text.toString()
        val password: String = binding.txtPassword.text.toString()
        val username: String = binding.txtUser.text.toString()

        firebaseStore = FirebaseFirestore.getInstance()

        Toast.makeText(this,email,Toast.LENGTH_SHORT).show()
        Toast.makeText(this,password,Toast.LENGTH_SHORT).show()
        Toast.makeText(this,username,Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                val uid = user?.uid
                // ID en la BBDD
                val userDB: DatabaseReference = dataBaseReference.child(uid!!)
                verifyEmail(user)

                dataBase.getReference("Usernames").child(username).setValue(email)

                userDB.child("id").setValue(uid)
                userDB.child("name").setValue(name)
                userDB.child("username").setValue(username)
                userDB.child("email").setValue(email)
                userDB.child("rol").setValue(rol)
                userDB.child("bio").setValue(binding.txtBio.text.toString())
                userDB.child("profilePhoto").setValue("")

                val data = hashMapOf(
                    "id" to uid,
                    "name" to name,
                    "username" to username,
                    "email" to email,
                    "rol" to rol,
                    "bio" to binding.txtBio.text.toString(),
                    "profilePhoto" to ""
                )
                firebaseStore.collection("Usuarios").document(uid).set(data).addOnSuccessListener {
                    Log.i("user-new","USER NEW")
                }.addOnFailureListener {
                }
            } else{
                auth.fetchSignInMethodsForEmail(binding.txt0Email.text.toString()).addOnCompleteListener(object:OnCompleteListener<SignInMethodQueryResult> {
                    override fun onComplete(p0: Task<SignInMethodQueryResult>) {
                        if(p0.isSuccessful) {
                            val checkEmail : Boolean = p0.result.signInMethods!!.isEmpty()
                            if(!checkEmail) {
                                Toast.makeText(applicationContext,"Ya existe una cuenta registrada con este correo electrónico",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun verifyUser(username:String,email:String) {
        var userRef = dataBase.getReference("Usernames")
        var verified : Boolean = false
        userRef.get().addOnCompleteListener(this) { task ->
            userRef.get().addOnSuccessListener { value ->
                if (!value.child(username).exists()) {
                    userRef.child(username).setValue(email)
                    Toast.makeText(applicationContext, "Username registrado", Toast.LENGTH_SHORT)
                        .show()
                    createAccount()
                    verified = true
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Username no disponible", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { Toast.makeText(this, "Username no disponible", Toast.LENGTH_SHORT).show() }
    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isComplete) {
                Toast.makeText(this, "Comprueba tu email.",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Oh, algo ha ido mal.", Toast.LENGTH_SHORT).show()
            }
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //go previous activity
        return super.onSupportNavigateUp()
    }
}