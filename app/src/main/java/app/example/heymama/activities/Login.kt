package app.example.heymama.activities

import PreferencesManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import app.example.heymama.Utils
import app.example.heymama.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Login : AppCompatActivity() {

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
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        if(prefs.isLogin()) {
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
                if (!Utils.isNetworkAvailable(this)) {
                    Utils.alertDialogInternet(this)
                } else if (Utils.isNetworkAvailable(this)) {
                logIn()}
            }
        }
    }

    /**
     * Este método permite iniciar sesión en la aplicación
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
     * Este método permite comprobar el registro del usuario
     * @param email FirebaseUser
     * @param mailVerified Boolean
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
     * @param email String
     * @param databaseReference DatabaseReference
     */
    private fun  checkRol(email:String, password:String)  {
        dataBaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    for (s in snapshot.children) {
                        if (s.child("email").value.toString() == email) {
                            rol = s.child("rol").value.toString()
                            prefs.createLoginSession(email, password, rol)
                            goHomeActivity(rol)
                        }
                    }
                    if(!snapshot.child(auth.uid.toString()).exists()) {
                        Utils.showToast(this@Login,"Tu cuenta ha sido inhabilitada. Crea una nueva cuenta.")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método permite iniciar la aplicación dependiendo del rol del usuario
     * @param rol String : Rol del usuario
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

}