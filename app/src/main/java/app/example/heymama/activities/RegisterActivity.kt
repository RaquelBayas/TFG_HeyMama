package app.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.ActionBar
import androidx.core.widget.doOnTextChanged
import app.example.heymama.R
import app.example.heymama.Utils
import app.example.heymama.databinding.ActivityRegisterBinding
import app.example.heymama.fragments.PoliticasFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var rol: String
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var firebaseStore: FirebaseFirestore
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

        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")

        focusUsername()
        focusEmail()
        focusPassword()
        focusName()
        binding.txtCheckBox.setOnClickListener {
            val fragment = PoliticasFragment()
            supportFragmentManager.beginTransaction().replace(R.id.activityRegister,fragment).addToBackStack(null).commit()
        }
        binding.btnCrearCuenta.setOnClickListener {
            validate()
        }
    }

    private fun validate() {
        val validUser = binding.userLayout.helperText == null
        val validName = binding.nameLayout.helperText == null
        val validEmail = binding.email0Layout.helperText == null
        val validPassword = binding.passwordLayout.helperText == null
        val checkBox = binding.checkBox.isChecked
        if(checkBox) {
            if (validUser && validName && validEmail && validPassword) {
                createAccount()
            } else {
                Utils.showToast(this, "Rellena los datos.")
            }
        } else {
            Utils.showToast(this, "Acepta los términos y política de privacidad.")
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
        val username = binding.txtUser.text.toString()
        val userRef = dataBase.getReference("Usernames")

        userRef.get().addOnCompleteListener(this) { task ->
            userRef.get().addOnSuccessListener { value ->
                if (!value.child(username.lowercase()).exists()) {
                    binding.userLayout.helperText = ""
                } else{
                    binding.userLayout.helperText = "Usuario no disponible"
                }
            }.addOnFailureListener {
                binding.userLayout.helperText = "Usuario no disponible"
                Utils.showToast(this, "Username no disponible")
            }
        }.addOnFailureListener { Utils.showToast(this, "Username no disponible") }
    }

    private fun focusName() {
        binding.txtName.setOnFocusChangeListener { view, focused ->
            if(!focused) {
                binding.nameLayout.helperText = validName()
            }
        }
    }

    private fun validName(): String? {
        val name = binding.txtName.text.toString()
        if(name.isEmpty()){
            return "Nombre inválido"
        }
        return null
    }

    private fun focusPassword() {
        binding.txtPassword.doOnTextChanged { text, start, before, count ->
            val password = binding.txtPassword.text.toString()
            if(password == null || password.length < 7){
                binding.passwordLayout.helperText = "Contraseña inválida"
            }else{
                binding.passwordLayout.helperText = null
            }
        }
    }

    private fun focusEmail() {
        binding.txt0Email.setOnFocusChangeListener { view, focused ->
            if(!focused) {
                binding.email0Layout.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = binding.txt0Email.text.toString()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Correo electrónico inválido"
        }
        return null
    }

    private fun createAccount() {
        val email: String = binding.txt0Email.text.toString()
        val name: String = binding.txtName.text.toString()
        val password: String = binding.txtPassword.text.toString()
        val username: String = binding.txtUser.text.toString()

        firebaseStore = FirebaseFirestore.getInstance()

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
                userDB.child("protected").setValue(false)
                userDB.child("status").setValue("offline")
                userDB.child("bio").setValue(binding.txtBio.text.toString())
                userDB.child("profilePhoto").setValue("")

                val data = hashMapOf(
                    "id" to uid,
                    "name" to name,
                    "username" to username,
                    "email" to email,
                    "rol" to rol,
                    "protected" to false,
                    "status" to "offline",
                    "bio" to binding.txtBio.text.toString(),
                    "profilePhoto" to ""
                )
                firebaseStore.collection("Usuarios").document(uid).set(data).addOnSuccessListener {

                }.addOnFailureListener {
                }
            } else{
                auth.fetchSignInMethodsForEmail(binding.txt0Email.text.toString()).addOnCompleteListener(object:OnCompleteListener<SignInMethodQueryResult> {
                    override fun onComplete(p0: Task<SignInMethodQueryResult>) {
                        if(p0.isSuccessful) {
                            val checkEmail : Boolean = p0.result.signInMethods!!.isEmpty()
                            if(!checkEmail) {
                                Utils.showToast(applicationContext,"Ya existe una cuenta registrada con este correo electrónico")
                            }
                        }
                    }
                })
            }
        }
    }

    private fun verifyEmail(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isComplete) {
                Utils.showToast(this, "Comprueba tu email.")
            } else {
                Utils.showToast(this, "Oh, algo ha ido mal.")
            }
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}