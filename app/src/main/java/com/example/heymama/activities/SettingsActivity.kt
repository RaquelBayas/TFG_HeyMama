package com.example.heymama.activities

import PreferencesManager
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivitySettingsBinding
import com.example.heymama.models.User
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.protobuf.Value


class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var currentUser: FirebaseUser
    private lateinit var uid: String
    private lateinit var rol: String
    private lateinit var switch: SwitchCompat
    private lateinit var prefs: PreferencesManager
    private lateinit var binding: ActivitySettingsBinding
    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)
        switch = binding.switchPrivacidad

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        currentUser = auth.currentUser!!
        uid = auth.uid.toString()

        getDataUser()

        binding.settingsName.setOnClickListener { changeName() }
        binding.settingsUsername.setOnClickListener { changeUsername() }
        binding.settingsBio.setOnClickListener { changeBio() }
        binding.settingsEmail.text = auth.currentUser!!.email.toString()
        binding.settingsEmail.setOnClickListener { changeEmail() }
        binding.btnDeleteAccount.setOnClickListener {
            deleteMyAccount()
        }
        binding.settingsPassword.setOnClickListener { changePassword() }
        binding.txtAcercade.setOnClickListener { startActivity(Intent(this,InfoAppActivity::class.java)) }
        changePrivacidad()
    }

    private fun deleteMyAccount() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_delete_account,null)
        builder.setView(view)

        val password = view.findViewById<EditText>(R.id.edt_password_delete_account)

        builder.setPositiveButton("Confirmar"){ _ : DialogInterface, which ->
            if(password.text.isEmpty()) {
                return@setPositiveButton
            } else {
                val credential : AuthCredential = EmailAuthProvider.getCredential(currentUser.email!!,password.text.toString())
                currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        deleteUserMood(uid)
                        deleteUserPosts(uid)
                        deleteUserLikes(uid)
                        deleteUserFriends(uid)
                        deleteForos(uid)
                        deleteUserChats(uid)
                        database.reference.child("Usuarios").child(uid).child("rol").get().addOnSuccessListener {
                            if(it.value == "Profesional") {
                                deleteUserArticulos(uid)
                            }
                        }
                        deleteChatList(uid)
                        deleteUserConsultas(uid)
                        deleteUserFriendRequests(uid)
                        deleteNotifications(uid)
                        deleteUserPhotos(uid)
                        val username = binding.settingsUsername.text.toString()
                        database.reference.child("Usernames").child(username).removeValue()
                        database.reference.child("Usuarios").child(uid).removeValue()
                        firestore.collection("Usuarios").document(uid).delete().addOnSuccessListener {
                            Log.i("firestore-user","ok")
                        }.addOnFailureListener {
                            Log.e("firestore-user",it.toString())
                        }
                        database.reference.child("Tokens").child(uid).removeValue()
                        deleteAccount()
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
     * Este método permite obtener el rol del usuario, el nombre de usuario, email y biografía.
     */
    private fun getDataUser(){
        database.reference.child("Usuarios").child(uid).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                    binding.settingsEmail.text = user.email.toString()
                    binding.settingsName.text = user.name.toString()
                    binding.settingsUsername.text = user.username.toString()
                    binding.settingsBio.text = user.bio.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity",error.toString())
            }
        })
    }

    /**
     * Este método permite al usuario cambiar la configuración de la privacidad de su perfil.
     * ON -> Perfil privado
     * OFF -> Perfil público
     */
    private fun changePrivacidad() {
        firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
            if(value!!.exists()) {
                switch.isChecked = value!!["protected"] as Boolean
            }
        }
        //switch.isChecked = prefs.isProtected()

        switch.setOnClickListener {
            if(switch.isChecked) {
                firestore.collection("Usuarios").document(uid).update("protected", true)
                database.getReference("Usuarios").child(uid).child("protected").setValue(true)
                prefs.switchPrivacidad(true)

            } else {
                firestore.collection("Usuarios").document(uid).update("protected", false)
                database.getReference("Usuarios").child(uid).child("protected").setValue(false)
                prefs.switchPrivacidad(false)
            }
            prefs.preferences!!.edit().apply()
        }
    }

    private fun changeName() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)

        //Obtenemos el editText del nombre de usuario
        val txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        val txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = resources.getString(R.string.old_name)
        txt_new.text = resources.getString(R.string.new_name)

        val new_name = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_name = view.findViewById<TextView>(R.id.settings_old_name)

        val user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("name").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    old_name.text = snapshot.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity",error.toString())
            }
        })

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_name.text.isEmpty()) {
                return@setPositiveButton
            } else {
                val bio: Map<String, String> = mapOf("name" to new_name.text.toString())
                user_ref.updateChildren(bio)
                database.getReference("Usuarios").child(uid).child("name").setValue(new_name.text.toString())
                firestore.collection("Usuarios").document(uid).update("name",new_name.text.toString())
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Este método permite al usuario cambiar su biografía.
     */
    private fun changeBio() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)

        val txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        val txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = resources.getString(R.string.bio_actual)
        txt_new.text = resources.getString(R.string.bio_nueva)

        val new_bio = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_bio = view.findViewById<TextView>(R.id.settings_old_name)

        val user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("bio").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    old_bio.text = snapshot.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity",error.toString())
            }
        })

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, which ->
            if(new_bio.text.isEmpty()) {
                return@setPositiveButton
            } else {
                val bio: Map<String, String> = mapOf("bio" to new_bio.text.toString())
                user_ref.updateChildren(bio)
                database.getReference("Usuarios").child(uid).child("bio").setValue(new_bio.text.toString())
                firestore.collection("Usuarios").document(uid).update("bio",new_bio.text.toString())
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
            dialogInterface.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Este método permite al usuario cambiar su contraseña.
     */
    private fun changePassword() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_password,null)
        builder.setView(view)

        val old_password = view.findViewById<TextView>(R.id.settings_current_password)
        val new_password = view.findViewById<EditText>(R.id.settings_new_password)
        val confirm_new_password = view.findViewById<EditText>(R.id.settings_confirm_new_password)

        builder.setPositiveButton("Confirmar"){ dialogInterface : DialogInterface, _ ->
            if(old_password.text.isNotEmpty() && new_password.text.isNotEmpty() && confirm_new_password.text.isNotEmpty()) {
                if(new_password.text.toString().equals(confirm_new_password.text.toString())) {
                    Toast.makeText(this,"Contraseñas iguales",Toast.LENGTH_SHORT).show()
                    val credential : AuthCredential = EmailAuthProvider.getCredential(currentUser.email!!,old_password.text.toString())
                    currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            currentUser.updatePassword(new_password.text.toString()).addOnCompleteListener { task ->
                                if(task.isSuccessful) {
                                    Toast.makeText(this,"Contraseña actualizada correctamente",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this,"No se ha podido actualizar la contraseña",Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this,"Contraseña actual incorrecta",Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this,"No coincide la nueva contraseña",Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancelar"){ dialogInterface : DialogInterface, which ->
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Este método permite al usuario cambiar de correo electrónico
     */
    private fun changeEmail() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_email,null)
        builder.setView(view)

        val txt_old = view.findViewById<TextView>(R.id.settings_txt_old)
        val txt_new = view.findViewById<TextView>(R.id.settings_txt_new)
        txt_old.text = "Correo electrónico actual"
        txt_new.text = "Nuevo correo electrónico"

        val new_email = view.findViewById<EditText>(R.id.edt_settings_email)
        val current_password = view.findViewById<EditText>(R.id.edt_settings_password)
        val old_email = view.findViewById<TextView>(R.id.settings_old_email)

        val user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("email").ref.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    old_email.text = snapshot.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("TAG",error.toString())
            }
        })

        builder.setPositiveButton("Confirmar"){ _ : DialogInterface, which ->
            if(new_email.text.isEmpty() || current_password.text.isEmpty()) {
                return@setPositiveButton
            } else {
                val users_ref = database.getReference("Usuarios")
                users_ref.addValueEventListener(object:ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        if(user!!.email == new_email.text.toString()) {
                            Toast.makeText(applicationContext,"Ya existe un usuario con ese correo electrónico",Toast.LENGTH_SHORT).show()
                        } else {
                            val credential : AuthCredential = EmailAuthProvider.getCredential(currentUser.email!!,current_password.text.toString())
                            currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    auth.currentUser!!.updateEmail(new_email.text.toString()).addOnSuccessListener {
                                        val email: Map<String, String> = mapOf("email" to new_email.text.toString())
                                        user_ref.updateChildren(email)
                                        firestore.collection("Usuarios").document(uid).update("email",new_email.text.toString())
                                        Toast.makeText(applicationContext,R.string.email_updated,Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener{
                                        Toast.makeText(applicationContext,"Se ha producido un error",Toast.LENGTH_SHORT).show()
                                        Log.e("SettingsActivity",it.toString())
                                    }
                                }
                            }

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.i("SettingsActivity",error.toString())
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
     * Este método permite al usuario cambiar su nombre de usuario.
     */
    private fun changeUsername() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_change_username,null)
        builder.setView(view)

        val new_username = view.findViewById<EditText>(R.id.edt_settings_username)
        val old_username = view.findViewById<TextView>(R.id.settings_old_name)

        val user_ref = database.reference.child("Usuarios").child(uid)
        user_ref.child("username").ref.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    old_username.text = snapshot.value.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity",error.toString())
            }
        })

        builder.setPositiveButton("Confirmar"){ _: DialogInterface, _ ->
            if(new_username.text.isEmpty()) {
                return@setPositiveButton
            } else {
                val usernames_ref = database.getReference("Usernames")
                usernames_ref.get().addOnSuccessListener { value ->
                    if(!value.child(new_username.text.toString()).exists()) {
                        val username: Map<String, String> = mapOf("username" to new_username.text.toString())
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
        }
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Este método permite al usuario eliminar su cuenta.
     */
    private fun deleteAccount() {
        currentUser.delete().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Toast.makeText(this,"Cuenta eliminada correctamente",Toast.LENGTH_SHORT).show()
                Log.i("DeleteAccount","Cuenta eliminada correctamente")
                logOut()
            } else {
                Toast.makeText(this,"Se ha producido un error.",Toast.LENGTH_SHORT).show()
                Log.e("DeleteAccount-exception",task.exception.toString())
            }
        }
    }

    /**
     * Este método elimina las notificaciones.
     * @param userId String : UID del usuario
     */
    private fun deleteNotifications(userId: String) {
        database.reference.child("NotificationsTL").child(userId).removeValue().addOnSuccessListener {
            Log.i("deleteNotifications","Notificaciones TL eliminadas")
        }
        database.reference.child("NotificationsConsultas").equalTo("uid",userId).ref.removeValue().addOnSuccessListener {
            Log.i("deleteNotifications","Notificaciones Consultas eliminadas")
        }
        var notiRef = database.reference.child("NotificationsTL")
        notiRef.get().addOnCompleteListener {
            it.result.children.iterator().forEach {
                it.children.iterator().forEach {
                    if(it.child("uid").value == userId) {
                        it.ref.removeValue()
                    }
                }
            }
        }
    }

    /**
     * Este método elimina los foros publicados y los comentarios.
     * @param userId String : UID del usuario
     */
    private fun deleteForos(userId: String) {
        val temasForos = arrayListOf("Depresión","Embarazo","Posparto","Otros")
        firestore.collection("Foros").document("SubForos").addSnapshotListener { value, error ->
            temasForos.iterator().forEach {
                value!!.reference.collection(it).whereEqualTo("userID",userId).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        it.reference.collection("Comentarios").addSnapshotListener { value, error ->
                            value!!.documents.iterator().forEach { it.reference.delete() }
                            it.reference.delete().addOnSuccessListener {
                                Log.i("deleteForos","OK")
                            }.addOnFailureListener {
                                Log.i("deleteForos",it.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Este método elimina las consultas del usuario
     * @param userId String: UID del usuario
     */
    private fun deleteUserConsultas(userId: String) {
        val arrayTemas = arrayListOf("Embarazo","Familia","Parto","Posparto","Otros")
        for(tema in arrayTemas) {
            firestore.collection("Consultas").document(tema).collection("Consultas").whereEqualTo("userID",userId).addSnapshotListener { value, error ->
                if(error != null) {
                    Log.e("SettingsActivity",error.toString())
                    return@addSnapshotListener
                }
                value!!.documents.iterator().forEach {
                    it.reference.collection("Respuestas").addSnapshotListener { value, error ->
                        if(error != null) {
                            Log.e("SettingsActivity",error.toString())
                        }
                        value!!.documents.iterator().forEach { it.reference.delete() }
                    }
                    it.reference.delete()
                    Log.i("deleteUserConsultas","OK")
                }
            }
        }
    }

    /**
     * Este método elimina los chats del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserChats(userId: String) {
        var chatsRef = database.reference.child("Chats")
        var chatListRef = database.reference.child("ChatList")
        chatListRef.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.iterator().forEach {
                        chatListRef.child(it.key.toString()).child(userId).removeValue()
                    }
                    Log.i("deleteUserChatList", "Chats eliminados")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        var chats = chatsRef.child(userId).child("Messages")
        chats.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    snapshot.children.iterator().forEach {
                        chatsRef.child(it.key.toString()).child("Messages").child(userId).removeValue()
                        it.ref.removeValue().addOnSuccessListener {
                        Log.i("deleteUserChats","OK")
                    } }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("SettingsActivity",error.toString())
            }
        })
    }

    /**
     * Este método elimina los registros de estado.
     * @param userId String : UID del usuario
     */
    private fun deleteUserMood(userId: String) {
        firestore.collection("Mood").document(userId).collection("Historial").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserMood","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserMood",it.toString())
                }
            }
        }
    }

    /**
     * Este método elimina los posts publicados por el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserPosts(userId: String) {
        var postsRef = firestore.collection("Timeline").whereEqualTo("userId",userId)
        postsRef.addSnapshotListener { value, error ->
            value?.documents?.iterator()?.forEach {
                it.reference.collection("Replies").addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach { it.reference.delete() }
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserPosts","OK")
                }.addOnFailureListener { Log.e("deleteUserPosts",it.toString()) }
            }
        }
        firestore.collection("Timeline").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.collection("Replies").whereEqualTo("userId",userId).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach { posts ->
                        posts.reference.delete().addOnSuccessListener {
                            Log.i("deleteUserPosts","OK-2")
                        }.addOnFailureListener { Log.e("deleteUserPosts",it.toString()) }
                    }
                }
            }
        }
    }

    /**
     * Este método elimina las fotos del usuario (perfil,layout).
     * @param userId String : UID del usuario
     */
    private fun deleteUserPhotos(userId: String) {
        var perfilRef = firebaseStore.getReference("Usuarios/$userId/images/perfil")
        perfilRef.downloadUrl.addOnSuccessListener {
            perfilRef.delete()
            Log.i("deleteUserPhotos","OK")
        }.addOnFailureListener {
            Log.e("deleteUserPhotos",it.toString())
        }
        var layoutRef = firebaseStore.getReference("Usuarios/$userId/images/layout")
        layoutRef.downloadUrl.addOnSuccessListener {Log.i("deleteUserPhotoLayou","Ok")
        }.addOnFailureListener {
            Log.e("deleteUserPhotos",it.toString())
        }
    }

    /**
     * Este método elimina los posts a los que ha dado 'like' el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserLikes(userId: String) {
        firestore.collection("Likes").document(userId).collection("Likes").addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("deleteUserLikes",error.toString())
                return@addSnapshotListener
            }
            value!!.documents.iterator().forEach {
                firestore.collection("PostsLiked").document(it.id).collection("Users").document(userId).delete()
                it.reference.delete().addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.i("deleteUserLikes","OK")
                    } else {
                        Log.i("deleteUserLikes",it.toString())
                    }
                }
            }
        }
    }

    /**
     * Este método elimina los chats del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteChatList(userId: String) {
        val ref = database.reference.child("ChatList")
        ref.child(userId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.removeValue().addOnCompleteListener {
                        if(it.isSuccessful) {
                            Log.i("deleteChatlist","OK")
                        } else {
                            Log.i("deleteChatlist",it.toString())
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método, en el caso de los profesionales, elimina los artículos publicados.
     * @param userId String : UID del usuario
     */
    private fun deleteUserArticulos(userId: String) {
        firestore.collection("Artículos").whereEqualTo("professionalID",userId).addSnapshotListener { value, error ->
            if(value!!.documents.isNotEmpty()){
            value!!.documents.iterator().forEach { it.reference.delete().addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.i("deleteUserArticulos","OK")
                } else {
                    Log.i("deleteUserArticulos",it.toString())
                }
            } } }
        }
    }

    /**
     * Este método elimina las solicitudes de amistad.
     * @param userId String : UID del usuario
     */
    private fun deleteUserFriendRequests(userId: String) {
        val reference = firestore.collection("Friendship")
        reference.document(userId).collection("FriendRequest").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                reference.document(it.id).collection("FriendRequest").document(userId).delete().addOnSuccessListener {
                    Log.i("deleteUserFriendRequest","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserFriendRequest",it.toString())
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserFriendRequest","OK-2")
                }.addOnFailureListener {
                    Log.i("deleteUserFriendRequest",it.toString())
                }
            }
        }
    }

    /**
     * Este método elimina los amigos del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserFriends(userId: String){
        val reference = firestore.collection("Friendship")
        reference.document(userId).collection("Friends").addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("deleteUserFriends-error",error.toString())
                return@addSnapshotListener
            }
            value!!.documents.iterator().forEach {
                reference.document(it.id).collection("Friends").document(userId).delete().addOnSuccessListener {
                    Log.i("deleteUserFriends","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserFriends",it.toString())
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserFriends","OK-2")
                }.addOnFailureListener {
                    Log.i("deleteUserFriends",it.toString())
                }
            }
        }
        reference.document(userId).delete()
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        Utils.updateStatus("offline")
    }

    private fun currentUser(userid: String?) {
        val editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit()
        editor.putString("currentUser", userid)
        editor.apply()
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        Utils.updateStatus("online")
        currentUser(uid)
    }

    /**
     * Este método permite cerrar la sesión
     */
    private fun logOut() {
        prefs.editor?.clear()
        prefs.editor?.commit()
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}