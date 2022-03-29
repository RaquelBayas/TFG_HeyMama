package com.example.heymama.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityPerfilBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.PostTimeline
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
import com.theartofdev.edmodo.cropper.CropImageView


class PerfilActivity : AppCompatActivity(), Utils, ItemRecyclerViewListener {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var binding : ActivityPerfilBinding
    private lateinit var uid: String
    private var code: Int = 0

    private lateinit var btn_mensajes: Button
    private lateinit var btn_amigos : Button

    private lateinit var recyclerViewTimeline: RecyclerView
    private lateinit var postsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterPostsTL: PostTimelineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference
        firestore = FirebaseFirestore.getInstance()

        btn_amigos = findViewById(R.id.btn_amigos)
        // Cambiar de perfil
        val intent = intent
        if (!intent.getStringExtra("UserUID").isNullOrEmpty()) {
            uid = intent.getStringExtra("UserUID")!!
            changeButtons()
        } else {
            uid = auth.currentUser?.uid!!
            // Cambiar imagen layout
            binding.layoutImage.setOnClickListener { selectImage(100) }

            // Cambiar imagen de perfil
            binding.profileImage.setOnClickListener { selectImage(200) }

        }
        Toast.makeText(this,uid,Toast.LENGTH_SHORT).show()


        var txt_user_perfil = findViewById<TextView>(R.id.txt_user_perfil)

        var profileImage : ImageView = findViewById(R.id.profile_image)
        if (profileImage.drawable == null) {
            loadPicture(uid!!)
        }


        firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
            txt_user_perfil.text = value?.data?.get("name").toString()
            Log.i("NAME_PERFIL", value?.data?.get("name").toString())
        }
        // BOTÓN MENSAJES
        btn_mensajes = findViewById(R.id.btn_mensajes)
        Log.i("btn_mensajes",btn_mensajes.text.toString())

        btn_mensajes.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("friendUID",uid)
            startActivity(intent)
        }

        btn_amigos.setOnClickListener {
            val intent = Intent(this, FriendsActivity::class.java)
            intent.putExtra("UID",uid)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> goToActivity(this,RespirarActivity::class.java)
            }
        }

        //RECYCLER VIEW
        loadPostsTL()
    }

    private fun loadPostsTL() {
        recyclerViewTimeline = findViewById(R.id.recyclerView_perfil)
        var layoutManager = LinearLayoutManager(this)
        recyclerViewTimeline.layoutManager = layoutManager
        recyclerViewTimeline.setHasFixedSize(true)
        postsTLArraylist = arrayListOf()


        firestore.collection("Timeline").whereEqualTo("userId",uid).addSnapshotListener { snapshots, e ->
            if (e!= null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    }
                    DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(
                        PostTimeline::class.java))
                }
            }
            postsTLArraylist.sort()
            adapterPostsTL = PostTimelineAdapter(this,postsTLArraylist,this)

            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    Toast.makeText(this@PerfilActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                }
            })
            recyclerViewTimeline.adapter = adapterPostsTL
            recyclerViewTimeline.setHasFixedSize(true)
        }
    }

    private fun changeButtons() {
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
        if(btn_amigos.text == "Añadir") {
            Log.i("UID-FRIENDS-btn-0",btn_amigos.text.toString())
            var docFriends = firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").document(uid)
            docFriends.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        btn_amigos.setOnClickListener {
                            Toast.makeText(this, "Solicitud enviada.", Toast.LENGTH_SHORT).show()
                            btn_amigos.text = "Solicitud enviada"
                            sendFriendRequest()
                        }
                    }
                }
            }
        }
        else {
            Log.i("UID-FRIENDS-btn",btn_amigos.text.toString())
            btn_amigos.setOnClickListener {
                val intent = Intent(this, FriendsActivity::class.java)
                intent.putExtra("UID",uid)
                startActivity(intent)
            }
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

    private fun uploadImage(storageReference: StorageReference,uri:Uri){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo foto...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.putFile(uri).
                addOnSuccessListener {
                    //binding.imageView14.setImageURI(ImageUri)
                    Toast.makeText(this,"Foto subida",Toast.LENGTH_SHORT).show()
                    if(progressDialog.isShowing) progressDialog.dismiss()
                }.addOnFailureListener{
                    if(progressDialog.isShowing) progressDialog.dismiss()
                    Toast.makeText(this,"Hubo un error",Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage(code: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent,code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            100 -> {
                if(resultCode == Activity.RESULT_OK) {
                    Log.i("IMAGEURI-1",requestCode.toString())
                    data?.data?.let {
                        code = 100
                        CropImage.activity(it).setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1500, 500)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .start(this)
                    }
                }
            }
            200 -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("IMAGEURI-2 ", requestCode.toString())
                    data?.data?.let {
                        code = 200
                        CropImage.activity(it).setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(400, 400)
                            .setCropShape(CropImageView.CropShape.RECTANGLE)
                            .start(this)
                    }
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                var result = CropImage.getActivityResult(data)
                if((resultCode == Activity.RESULT_OK) && (code==100)) {
                    Log.i("IMAGEURI",requestCode.toString())
                    binding.layoutImage.setImageURI(result.uri)
                    storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/layout")
                    uploadImage(storageReference,result.uri)
                    code = 0
                } else if((resultCode == Activity.RESULT_OK) && (code==200)) {
                    Log.i("IMAGEURI",requestCode.toString())
                    binding.profileImage.setImageURI(result.uri)
                    storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/perfil")
                    uploadImage(storageReference,result.uri)
                    code = 0
                }
                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.i("TAG","CROP ERROR: "+result.error.toString())
                }
            }
        }
    }
}