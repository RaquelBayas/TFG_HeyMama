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
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.FriendRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PerfilActivity : AppCompatActivity(), Utils {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var binding : ActivityPerfilBinding
    lateinit var ImageUri: Uri
    lateinit var uid: String

    lateinit var btn_mensajes: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser
        uid = auth.currentUser?.uid!!
        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        // Cambiar de perfil
        val intent = intent
        if (!intent.getStringExtra("UserUID").isNullOrEmpty()) {
            uid = intent.getStringExtra("UserUID")!!
            changeButtons()
        } else {
            // Cambiar imagen layout
            binding.layoutImage.setOnClickListener { selectImage(100) }

            // Cambiar imagen de perfil
            binding.profileImage.setOnClickListener { selectImage(200) }

        }
        Toast.makeText(this,uid,Toast.LENGTH_SHORT).show()


        var txt_user_perfil = findViewById<TextView>(R.id.txt_user_perfil)
        dataBaseReference.child(uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    txt_user_perfil.text = snapshot.child("name").getValue().toString() //!!.documents.get(0).get("Name").toString()  // Name - name
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext," error",Toast.LENGTH_SHORT).show()
            }

        })

        var profileImage : ImageView = findViewById(R.id.profile_image)
        if (profileImage.drawable == null) {
            loadPicture(uid!!)
        }

        firestore = FirebaseFirestore.getInstance()

        // BOTÓN MENSAJES
        btn_mensajes = findViewById(R.id.btn_mensajes)
        Log.i("btn_mensajes",btn_mensajes.text.toString())

        btn_mensajes.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("friendUID",uid)
            startActivity(intent)
        }


        val btn_amigos: Button = findViewById(R.id.btn_amigos)
        btn_amigos.setOnClickListener {
            goToActivity(this, FriendsActivity::class.java)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> goToActivity(this,RespirarActivity::class.java)
            }
        }

    }

    private fun changeButtons() {
        var btn_amigos : Button = findViewById(R.id.btn_amigos)
        var docFriends = firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").document(uid)
        docFriends.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()){
                    btn_amigos.text = "Amiga"

                    btn_mensajes.text = "Enviar mensaje"

                } else {
                    btn_amigos.text = "Añadir"
                }
            }
        }

        btn_amigos.setOnClickListener {
            Toast.makeText(this,"Solicitud enviada.",Toast.LENGTH_SHORT).show()
            btn_amigos.text = "Solicitud enviada"
            sendFriendRequest()
        }


    }


    fun sendFriendRequest() {
        // Comprobar si el user actual y el user del perfil visitado son amigos, sino enviar peticion

        var friendRequest_send = FriendRequest(uid,auth.currentUser?.uid.toString(),"send")
        var friendRequest_receive = FriendRequest(uid,auth.currentUser?.uid.toString(),"receive")

        firestore.collection("Friendship").document(auth.currentUser?.uid.toString()).collection("FriendRequest")
            .document(uid).set(friendRequest_send)
        firestore.collection("Friendship").document(uid).collection("FriendRequest")
            .document(auth.currentUser?.uid.toString()).set(friendRequest_receive)

    }

    fun loadPicture(uid: String) {
        // Comprueba si existe imagen de perfil en la bbdd
        var profileImage : ImageView = findViewById(R.id.profile_image)
        var layoutImage : ImageView = findViewById(R.id.layout_image)

        glidePicture(uid,"perfil",profileImage)
        glidePicture(uid,"layout",layoutImage)

    }

    fun glidePicture(uid: String, path:String, image:ImageView) {
        storageReference = firebaseStore.getReference("/Usuarios/"+uid+"/images/"+path)

        GlideApp.with(applicationContext)
            .load(storageReference)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }


    /*fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        activity.finish()
    }*/


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