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

class RegisterProfesionalActivity : AppCompatActivity() {

    private lateinit var txt_email_prof: EditText
    private lateinit var txt_user_prof: EditText
    private lateinit var txt_nombre_prof: EditText
    private lateinit var txt_apellidos_prof: EditText
    private lateinit var txt_password_prof: EditText
    private lateinit var btn_registro: Button

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_profesional)

        val bundle: Bundle? = intent.extras
        val rol: String? = intent.getStringExtra("Profesional")

        var actionBar: ActionBar? = supportActionBar
        actionBar?.setTitle("Create account")

        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)

        txt_email_prof = findViewById(R.id.txt_email_prof)
        txt_user_prof = findViewById(R.id.txt_user_prof)
        txt_nombre_prof = findViewById(R.id.txt_nombre_prof)
        txt_apellidos_prof = findViewById(R.id.txt_apellidos_prof)
        txt_password_prof = findViewById(R.id.txt_password_prof)
        btn_registro = findViewById(R.id.btn_crear_cuenta_prof)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        findViewById<Button>(R.id.btn_crear_cuenta_prof).setOnClickListener{
            createAccount()

        }
    }

    // Crear cuenta
    /**
     *
     * @param input
     *
     */
    private fun createAccount() {
        val email: String = txt_email_prof.text.toString()
        val password: String = txt_password_prof.text.toString()
        val user_prof: String = txt_user_prof.text.toString()
        val nombre_prof: String = txt_nombre_prof.text.toString()
        val apellidos_prof: String = txt_apellidos_prof.text.toString()

        firebaseStore = FirebaseFirestore.getInstance()

        Toast.makeText(this,email, Toast.LENGTH_SHORT).show()
        Toast.makeText(this,password, Toast.LENGTH_SHORT).show()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(user_prof)
            && !TextUtils.isEmpty(nombre_prof) && !TextUtils.isEmpty(apellidos_prof)) {

            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this,"Here.", Toast.LENGTH_SHORT).show()

                        // Usuario
                        val user: FirebaseUser? = auth.currentUser
                        val uid = user?.uid
                        // ID en la BBDD
                        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

                        verifyEmail(user)

                        userDB.child("id").setValue(uid)
                        userDB.child("name").setValue(nombre_prof)
                        userDB.child("apellidos").setValue(apellidos_prof)
                        userDB.child("user").setValue(user_prof)
                        userDB.child("email").setValue(email)
                        userDB.child("rol").setValue("Profesional")
                        userDB.child("bio").setValue("")
                        userDB.child("profilePhoto").setValue("")

                        val data = hashMapOf(
                            "ID" to uid,
                            "name" to nombre_prof,
                            "surname" to apellidos_prof,
                            "username" to user_prof,
                            "email" to email,
                            "rol" to "Profesional",
                            "bio" to "",
                            "profilePhoto" to ""
                        )

                        val usuario = User(uid,nombre_prof,user_prof,email,"Profesional","","")
                        firebaseStore.collection("Usuarios").document(uid!!).set(data)
                        firebaseStore.collection("Usuarios").document(uid!!).set(usuario)

                    } else{
                        Toast.makeText(this,"Ocurrió un error al enviar el email de verificación.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Rellena los datos por favor.", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     *
     * @param user FirebaseUser
     *
     */
    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                if (task.isComplete) {
                    Toast.makeText(this, "Comprueba tu email.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Oh, algo ha ido mal.", Toast.LENGTH_SHORT).show()
                }

                val intent = Intent(this, MainActivity::class.java)
                //intent.putExtra("Profesional","Profesional")
                startActivity(intent)
            }

    }

    // Comprobar la contraseña


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() //go previous activity
        return super.onSupportNavigateUp()
    }
}
