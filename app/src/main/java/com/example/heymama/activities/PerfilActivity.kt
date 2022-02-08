package com.example.heymama.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ActivityPerfilBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PerfilActivity : AppCompatActivity() {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var binding : ActivityPerfilBinding
    lateinit var ImageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser
        val uid = auth.currentUser?.uid

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

        loadPicture()

        // Cambiar imagen layout
        binding.layoutImage.setOnClickListener { selectImage(100) }

        // Cambiar imagen de perfil
        binding.profileImage.setOnClickListener { selectImage(200) }

        var txt_user_perfil: TextView = findViewById(R.id.txt_user_perfil)
        txt_user_perfil.text = userDB.child("User").get().toString()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> goToActivity(this,RespirarActivity::class.java)
            }
        }

    }

    fun loadPicture() {
        // Comprueba si existe imagen de perfil en la bbdd
        var profileImage : ImageView = findViewById(R.id.profile_image)
        var layoutImage : ImageView = findViewById(R.id.layout_image)

        storageReference = firebaseStore.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/perfil")

        if(storageReference.downloadUrl.toString() != null) {
            GlideApp.with(applicationContext)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profileImage)
        }
        storageReference = firebaseStore.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/layout")
        if(storageReference.downloadUrl.toString() != null) {
            GlideApp.with(applicationContext)
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(layoutImage)
        }
        Log.i("TAG: ", "Download URL : " + storageReference);

    }

    fun onClick(view: Int) {
        when(view) {

            //R.id.button4 -> goToActivity(this, PerfilActivity::class.java)
        }

    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        activity.finish()
    }


    private fun uploadImage(storageReference: StorageReference){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo foto...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.putFile(ImageUri).
                addOnSuccessListener {
                    //binding.imageView14.setImageURI(ImageUri)
                    Toast.makeText(this,"Foto subida",Toast.LENGTH_SHORT).show()
                    if(progressDialog.isShowing) progressDialog.dismiss()
                }.addOnFailureListener{
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    Toast.makeText(this,"Hubo un error",Toast.LENGTH_SHORT).show()
        }
        //loadPicture()
    }

    private fun selectImage(code: Int) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent,code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && data != null) {
            ImageUri = data?.data!!
            if (requestCode==100) {
                binding.layoutImage.setImageURI(ImageUri)
                storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/layout")
                uploadImage(storageReference)
            } else if (requestCode==200) {
                binding.profileImage.setImageURI(ImageUri)
                storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/perfil")
                uploadImage(storageReference)
            }
        }
    }
}