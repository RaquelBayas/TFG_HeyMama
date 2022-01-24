package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity() {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var storageReference: StorageReference

    private lateinit var rol: String
    private lateinit var textView: TextView
    private lateinit var email: String
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        val user: FirebaseUser? = auth.currentUser
        // ID en la BBDD
        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference
        loadPicture()
        user?.let {
            for(profile in it.providerData) {
                val providerId = profile.providerId
                val uid = profile.uid
                email = profile.email.toString()
                //name = profile.displayName.toString()

            }
        }

        checkRol(email,dataBaseReference)
        //textView = findViewById(R.id.textView)
        //textView.text = name

        var btn_phone : Button = findViewById(R.id.btn_phone)
        btn_phone.setOnClickListener{
            onClick(R.id.btn_phone)
        }

        var btn_respirar : Button = findViewById(R.id.btn_respirar)
        btn_respirar.setOnClickListener{
            onClick(R.id.btn_respirar)
        }

        var txt_foros : TextView = findViewById(R.id.txt_foros)
        txt_foros.setOnClickListener{
            onClick(R.id.txt_foros)
        }


        var profile_image_home : CircleImageView = findViewById(R.id.profile_image_home)
        profile_image_home.setOnClickListener {
            onClick(R.id.profile_image_home)
        }

    }

    fun loadPicture() {
        // Comprueba si existe imagen de perfil en la bbdd
        var profileImage : ImageView = findViewById(R.id.profile_image_home)

        storageReference = firebaseStore.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/perfil")

        GlideApp.with(applicationContext)
            .load(storageReference)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(profileImage)
        /*if(storageReference.downloadUrl.toString() != null) {
            GlideApp.with(applicationContext)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profileImage)
        }*/
    }

    private fun checkRol(email:String, databaseReference: DatabaseReference)  {

        databaseReference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(s in snapshot.children) {
                    if (s.child("Email").value.toString().equals(email)) {
                        rol = s.child("Rol").value.toString()
                        if (rol.equals("Profesional")) {
                            Log.d("TAG Profesional: ", rol)
                        } else {
                            Log.d("TAG Usuario: ", rol)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun onClick(view: Int) {
        when(view) {
            R.id.btn_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.txt_foros -> goToActivity(this, ForosActivity::class.java)
            R.id.profile_image_home -> goToActivity(this, PerfilActivity::class.java)
            R.id.btn_phone -> goToActivity(this, ContactoActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        //activity.finish()
    }


}
/*
private fun View.setOnClickListener(btnIconProfile: Int) {

}*/
