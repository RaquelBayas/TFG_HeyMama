package com.example.heymama.activities

import PreferencesManager
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivitySettingsBinding
import com.example.heymama.models.Message
import com.example.heymama.models.User
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore


class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    private lateinit var currentUser: FirebaseUser
    private lateinit var uid: String
    private lateinit var rol: String
    private lateinit var prefs: PreferencesManager
    private lateinit var binding: ActivitySettingsBinding
    private var protected: Boolean = false
    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)

        if(prefs.isProtected()) {
            protected = prefs!!.preferences!!.getBoolean("IS_PROTECTED",false)
        }
        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser!!
        uid = auth.uid.toString()

        getDataUser()

        binding.settingsName.setOnClickListener {
            changeUsername()
        }

        binding.settingsBio.setOnClickListener {
            changeBio()
        }
        binding.settingsEmail.text = auth.currentUser!!.email.toString()
        binding.settingsEmail.setOnClickListener {
            changeEmail()
        }

        binding.btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        binding.settingsPassword.setOnClickListener {
            changePassword()
        }

        binding.txtAcercade.setOnClickListener {
            startActivity(Intent(this,InfoAppActivity::class.java))
        }
        changePrivacidad()
    }

    /**
     * Obtener el rol del usuario y el nombre
     *
     */
    private fun getDataUser(){
        database.reference.child("Usuarios").child(uid).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                rol = user!!.rol.toString()
                binding.settingsEmail.text = user!!.email.toString()
                binding.settingsName.text = user!!.username.toString()
                binding.settingsBio.text = user!!.bio.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
    }

    private fun changePrivacidad() {
        val switch = binding.switchPrivacidad
        switch.setOnCheckedChangeListener { compoundButton, b ->
            if(b) {
                firestore.collection("Usuarios").document(uid).update("protected", b)
                database.getReference("Usuarios").child(uid).child("protected").setValue(b)
                prefs.switchPrivacidad(b)
            }
        }

    }

    private fun changeBio() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)

        //Obtenemos el editText del nombre de usuario
        var txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        var txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = resources.getString(R.string.bio_actual)
        txt_new.text = resources.getString(R.string.bio_nueva)

        val new_bio = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_bio = view.findViewById<TextView>(R.id.settings_old_name)

        var user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("bio").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                old_bio.text = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_bio.text.isEmpty()) {
                return@setPositiveButton
            } else {
                var bio: Map<String, String> = mapOf("bio" to new_bio.text.toString())
                user_ref.updateChildren(bio)
                database.getReference("Usuarios").child(uid).child("bio").setValue(new_bio.text.toString())
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     *
     * @param input
     */
    private fun changePassword() {
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
                    val credential : AuthCredential = EmailAuthProvider.getCredential(currentUser.email!!,old_password.text.toString())
                    currentUser?.reauthenticate(credential).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            Log.d("TAG","Re-Authentication success")
                            currentUser?.updatePassword(new_password.text.toString()).addOnCompleteListener { task ->
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
    private fun changeEmail() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_email,null)
        builder.setView(view)

        //Obtenemos el editText del nombre de usuario
        var txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        var txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = "Correo electrónico actual"//R.string.settings_old_email
        txt_new.text = "Nuevo correo electrónico" //R.string.settings_new_email.toString()

        val new_email = view.findViewById<EditText>(R.id.edt_settings_email)
        val current_password = view.findViewById<EditText>(R.id.edt_settings_password)
        val old_email = view.findViewById<TextView>(R.id.settings_old_email)

        var user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("email").ref.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                old_email.text = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_email.text.isEmpty() || current_password.text.isEmpty()) {
                return@setPositiveButton
            } else {
                var users_ref = database.getReference("Usuarios")
                users_ref.addValueEventListener(object:ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        if(user!!.email == new_email.text.toString()) {
                            Toast.makeText(applicationContext,"Ya existe un usuario con ese correo electrónico",Toast.LENGTH_SHORT).show()
                        } else {
                            Log.i("newemail",new_email.text.toString())
                            val credential : AuthCredential = EmailAuthProvider.getCredential(currentUser.email!!,current_password.text.toString())
                            currentUser?.reauthenticate(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.currentUser!!.updateEmail(new_email.text.toString()).addOnSuccessListener {
                                        var email: Map<String, String> = mapOf("email" to new_email.text.toString())
                                        user_ref.updateChildren(email)
                                        Toast.makeText(applicationContext,R.string.email_updated,Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener{
                                        Log.i("email-up",it.message.toString())
                                        Toast.makeText(applicationContext,"Se ha producido un error",Toast.LENGTH_SHORT).show()
                                    }
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
    private fun changeUsername() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)

        //Obtenemos el editText del nombre de usuario
        val new_username = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_username = view.findViewById<TextView>(R.id.settings_old_name)

        var user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("username").ref.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                old_username.text = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        builder.setPositiveButton("Confirmar"){ _: DialogInterface, _ ->
            if(new_username.text.isEmpty()) {
                return@setPositiveButton
            } else {
                var usernames_ref = database.getReference("Usernames")
                usernames_ref.get().addOnSuccessListener { value ->
                    if(!value.child(new_username.text.toString()).exists()) {
                        var username: Map<String, String> = mapOf("username" to new_username.text.toString())
                        user_ref.updateChildren(username)
                        usernames_ref.child(old_username.text.toString()).removeValue()
                        usernames_ref.child(new_username.text.toString()).setValue(auth.currentUser!!.email)
                    } else {
                        Toast.makeText(this,"Ya existe un usuario con este nombre de usuario",Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                }
            }
        }
        builder.setNegativeButton("Cancelar"){ _ : DialogInterface, _ ->
            //
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     *
     * @param input
     *
     */
    private fun deleteAccount() {
        //firestore.collection("Chats").document(auth.uid.toString()).delete()

        var temasForos = arrayListOf<String>("Depresión","Embarazo","Posparto","Otros")
        for(temaForo in temasForos) {
            firestore.collection("Foros").document("SubForos").collection(temaForo).whereEqualTo("userID",auth.uid.toString())
                .addSnapshotListener { value, error ->
                    for(doc in value!!.documents) {
                        doc.reference.delete().addOnSuccessListener {
                            doc.reference.collection("Comentarios").addSnapshotListener { value, error ->
                               for (doc in value!!.documents) {
                                   doc.reference.delete()
                               }
                            }
                        }.addOnFailureListener {

                        }
                    }
                    Log.i("DELETE",value!!.documents.toString())
                }
        }

        firestore.collection("Mood").document(auth.uid.toString()).collection("Historial")
            .addSnapshotListener { value, error ->
                for(doc in value!!.documents) {
                    doc.reference.delete()
                }
            }
        firestore.collection("Timeline").whereEqualTo("userId",auth.uid.toString()).addSnapshotListener { value, error ->
            for(doc in value!!.documents) {
                doc.reference.delete()
            }
        }

        firestore.collection("Usuarios").document(auth.uid.toString()).delete().addOnSuccessListener {
            Toast.makeText(this,"Cuenta eliminada correctamente",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Utils.showError(this,"Se ha producido un error")
        }
    }


}