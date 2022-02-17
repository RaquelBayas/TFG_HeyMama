package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.heymama.R
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class RegisterActivity : AppCompatActivity() {

    lateinit var txt_email: EditText
    lateinit var txt_password: EditText
    lateinit var txt_user: EditText
    lateinit var txt_name: EditText
    private lateinit var btn_registro: Button

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    lateinit var firebaseStore: FirebaseFirestore
    lateinit var storageReference: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val bundle: Bundle? = intent.extras
        val rol: String? = intent.getStringExtra("Usuario")

        var actionBar: ActionBar? = supportActionBar
        actionBar?.setTitle("Create account")

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        txt_email = findViewById(R.id.txt_email)
        txt_password = findViewById(R.id.txt_password)
        txt_user = findViewById(R.id.txt_user)
        txt_name = findViewById(R.id.txt_name)
        btn_registro = findViewById(R.id.btn_crear_cuenta)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        findViewById<Button>(R.id.btn_crear_cuenta).setOnClickListener{
            createAccount()
        }

    }

    // Crear cuenta
    private fun createAccount() {
        val email: String = txt_email.text.toString()
        val name: String = txt_name.text.toString()
        val password: String = txt_password.text.toString()
        val username: String = txt_user.text.toString()

        if (!verifyPassword(password)) {
            Toast.makeText(this,applicationContext.getString(R.string.ContraseñaInsegura),Toast.LENGTH_SHORT).show()
        }

        firebaseStore = FirebaseFirestore.getInstance()

        Toast.makeText(this,email,Toast.LENGTH_SHORT).show()
        Toast.makeText(this,password,Toast.LENGTH_SHORT).show()
        Toast.makeText(this,username,Toast.LENGTH_SHORT).show()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {

            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this,"Here again",Toast.LENGTH_SHORT).show()

                        // Usuario
                        val user: FirebaseUser? = auth.currentUser
                        val uid = user?.uid
                        // ID en la BBDD
                        val userDB: DatabaseReference = dataBaseReference.child(uid!!)

                        verifyEmail(user)

                        Toast.makeText(this,"Here again 2 !",Toast.LENGTH_SHORT).show()

                        userDB.child("id").setValue(uid)
                        userDB.child("name").setValue(name)
                        userDB.child("user").setValue(username)
                        userDB.child("Email").setValue(email)
                        userDB.child("Rol").setValue("Usuario")
                        userDB.child("bio").setValue("")
                        userDB.child("profilePhoto").setValue("")
                        //userDB.child("Password").setValue(password)

                        val data = hashMapOf(
                            "ID" to uid,
                            "Name" to name,
                            "Username" to username,
                            "Email" to email,
                            "Rol" to "Usuario",
                            "Bio" to "",
                            "profilePhoto" to ""
                        )

                        //val usuario = User(auth.uid.toString(),name,username,email,"Usuario","","")
                        firebaseStore.collection("Usuarios").document(uid).set(data)
                        //firebaseStore.collection("Usuarios").document(auth.uid.toString()).set(usuario)


                    } else{
                        Toast.makeText(this,"Ocurrió un error al enviar el email de verificación.",Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Rellena los datos por favor.",Toast.LENGTH_SHORT).show()
        }

    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isComplete) {
                    Toast.makeText(this, "Comprueba tu email.",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Oh, algo ha ido mal.", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent(this, MainActivity::class.java)
                //intent.putExtra("Usuario","Usuario")
                startActivity(intent)
            }
        Toast.makeText(this, "VerifyEmail 2",Toast.LENGTH_SHORT).show()
    }

    // Comprobar la contraseña
    private fun verifyPassword(password: String) : Boolean{
        return (password != null && password.length >= 7)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //go previous activity
        return super.onSupportNavigateUp()
    }
}