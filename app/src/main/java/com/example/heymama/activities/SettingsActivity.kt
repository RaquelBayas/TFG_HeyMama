package com.example.heymama.activities

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
import com.example.heymama.models.User
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore


class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    private lateinit var user: FirebaseUser
    private lateinit var uid: String
    private lateinit var rol: String

    private lateinit var binding: ActivitySettingsBinding

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        user = auth.currentUser!!
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
                binding.settingsName.text = user!!.name.toString()
                binding.settingsBio.text = user!!.bio.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
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

        var user_ref = firestore.collection("Usuarios").document(uid)
        user_ref.addSnapshotListener { value, error ->
            old_bio.text = value!!.data!!.get("bio").toString()
        }

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_bio.text.isEmpty()) {
                return@setPositiveButton
            } else {
                user_ref.update("bio",new_bio.text.toString())
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
    private fun changeEmail() {
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
                var users_ref = database.getReference("Usuarios")
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
    private fun changeUsername() {
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
                var usernames_ref = database.getReference("Usernames")
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