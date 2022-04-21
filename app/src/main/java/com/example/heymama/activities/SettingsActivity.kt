package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.heymama.R
import com.example.heymama.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var uid: String

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")
        user = auth.currentUser!!
        uid = auth.uid.toString()

        val txt_settings_name : TextView = findViewById(R.id.settings_name)
        txt_settings_name.setOnClickListener {
            change_username()
        }

        var user_ref = firestore.collection("Usuarios").document(uid)
        user_ref.addSnapshotListener { value, error ->
            txt_settings_name.text = value!!.data!!.get("username").toString()
        }


        val txt_settings_email : TextView = findViewById(R.id.settings_email)
        txt_settings_email.text = auth.currentUser!!.email.toString()
        txt_settings_email.setOnClickListener {
            change_email()
        }

        val btn_delete_account : Button = findViewById(R.id.btn_delete_account)
        btn_delete_account.setOnClickListener {
            delete_account()
        }

        val txt_settings_password : TextView = findViewById(R.id.settings_password)
        txt_settings_password.setOnClickListener {
            change_password()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> {
                    goToActivity(this,RespirarActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_foros -> {goToActivity(this,ForosActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_home -> {
                    finish()
                    goToActivity(this,HomeActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

    }

    private fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }

    private fun change_password() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_password,null)
        builder.setView(view)

        val old_password = view.findViewById<TextView>(R.id.settings_current_password)
        val new_password = view.findViewById<EditText>(R.id.settings_new_password)
        val confirm_new_password = view.findViewById<EditText>(R.id.settings_confirm_new_password)

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(old_password.text.isNotEmpty() && new_password.text.isNotEmpty() && confirm_new_password.text.isNotEmpty()) {
                if(new_password.text.toString().equals(confirm_new_password.text.toString())) {
                    Toast.makeText(this,"Contraseñas iguales",Toast.LENGTH_SHORT).show()
                    val credential : AuthCredential = EmailAuthProvider.getCredential(user.email!!,old_password.text.toString())
                    user?.reauthenticate(credential).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Log.d("TAG","Re-Authentication success")
                            user?.updatePassword(new_password.text.toString()).addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    Toast.makeText(this,"Contraseña actualizada correctamente",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this,"No se ha podido actualizar la contraseña",Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Log.d("TAG","Re-Authentication failed")
                        }
                    }
                } else {
                    Toast.makeText(this,"No coincide la nueva contraseña",Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            //dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     *
     * @param input
     *
     */
    private fun change_email() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)


        //Obtenemos el editText del nombre de usuario
        var txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        var txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = "Correo electrónico actual"//R.string.settings_old_email
        txt_new.text = "Nuevo correo electrónico" //R.string.settings_new_email.toString()

        val new_email = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_email = view.findViewById<TextView>(R.id.settings_old_name)

        var user_ref = firestore.collection("Usuarios").document(uid)
        user_ref.addSnapshotListener { value, error ->
            old_email.text = value!!.data!!.get("email").toString()
        }


        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_email.text.isEmpty()) {
                return@setPositiveButton
            } else {
                var users_ref = dataBase.getReference("Usuarios")
                user_ref.addSnapshotListener { value, error ->
                    var data = value!!.data
                   if (data!!.get("email")!!.equals(new_email.text.toString())) {
                        Log.i("email",data.get("email").toString())
                    }
                }
                /*auth.currentUser!!.updateEmail(new_email.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,R.string.email_updated,Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this,task.exception!!.message.toString(),Toast.LENGTH_SHORT).show()
                    }
                }*/
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            //dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     *
     * @param input
     *
     */
    private fun change_username() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)


        //Obtenemos el editText del nombre de usuario
        val new_username = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_username = view.findViewById<TextView>(R.id.settings_old_name)

        var user_ref = firestore.collection("Usuarios").document(uid)
        user_ref.addSnapshotListener { value, error ->
            old_username.text = value!!.data!!.get("username").toString()
        }

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_username.text.isEmpty()) {
                return@setPositiveButton
            } else {
                var usernames_ref = dataBase.getReference("Usernames")
                usernames_ref.get().addOnSuccessListener { value ->
                    if(!value.child(new_username.text.toString()).exists()) {
                        user_ref.update("username",new_username.text.toString())
                        usernames_ref.child(old_username.text.toString()).removeValue()
                        usernames_ref.child(new_username.text.toString()).setValue(auth.currentUser!!.email)
                    } else {
                        Toast.makeText(this,"Ya existe un usuario con este nombre de usuario",Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                }
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            //dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     *
     * @param input
     *
     */
    private fun delete_account() {

    }


}