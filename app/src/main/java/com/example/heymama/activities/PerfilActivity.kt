package com.example.heymama.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityPerfilBinding
import com.example.heymama.fragments.LikesFragment
import com.example.heymama.fragments.TimelineFragment
import com.example.heymama.fragments.ViewPagerAdapter
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
    private lateinit var currentUserUID: String
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var binding : ActivityPerfilBinding
    private lateinit var uid: String
    private var code: Int = 0

    private lateinit var btn_mensajes: Button
    private lateinit var btn_amigos : Button
    private lateinit var profileImage: ImageView
    private lateinit var layoutImage: ImageView
    private lateinit var txt_user_perfil: TextView
    private lateinit var txt_user_biografia : TextView
    private lateinit var recyclerViewTimeline: RecyclerView
    private lateinit var postsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterPostsTL: PostTimelineAdapter

    /**
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        currentUserUID = auth.currentUser!!.uid

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference

        btn_amigos = binding.btnAmigos

        // Cambiar de perfil
        val intent = intent

        checkUserProfile()

        txt_user_perfil = binding.txtUserPerfil
        txt_user_biografia = binding.txtBiografia
        profileImage = binding.profileImage
        if (profileImage.drawable == null) {
            loadPicture(uid!!)
        }

        firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
            txt_user_perfil.text = value?.data?.get("name").toString()
            txt_user_biografia.text = value?.data?.get("bio").toString()
        }
        // BOTÓN MENSAJES
        btn_mensajes = binding.btnMensajes
        if(uid == auth.currentUser!!.uid) {
            btn_mensajes.setOnClickListener {
                val intent = Intent(this, ListChatsActivity::class.java)
                intent.putExtra("friendUID", uid)
                startActivity(intent)
            }
        } else {
            btn_mensajes.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("friendUID", uid)
                startActivity(intent)
            }
        }

        if(currentUserUID != uid) {
            request()
        }
        btn_amigos.setOnClickListener {
            Log.i("TAG-amigos-perfil",btn_amigos.text.toString())
            checkButtonFriends(btn_amigos.text.toString())
        }

        setUpTabs()
    }

    private fun checkButtonFriends(btn_amigos_text: String) {
        when(btn_amigos_text) {
            "Amiga" -> {
                val intent = Intent(this, FriendsActivity::class.java)
                intent.putExtra("UID", uid)
                startActivity(intent)
            }
            "Añadir" -> {
                sendFriendRequest()
                btn_amigos.setText("Solicitud enviada")
            }
            "Solicitud enviada" -> {
                var dialog = AlertDialog.Builder(this@PerfilActivity)
                dialog.setTitle("Cancelar solicitud de amistad")
                dialog.setMessage("¿Deseas cancelar la solicitud de amistad?")
                dialog.setPositiveButton("Cancelar solicitud") { dialog, which ->
                    btn_amigos.setText("Añadir")
                    cancelRequest()
                }
                dialog.setNegativeButton("Volver atrás") { dialog, which ->
                    Toast.makeText(applicationContext,
                        "", Toast.LENGTH_SHORT).show()
                }
                dialog.show()
            }
            "Responder solicitud" -> {
                val popupmenu: PopupMenu = PopupMenu(this, btn_amigos)
                popupmenu.menuInflater.inflate(R.menu.request_friends_menu, popupmenu.menu)
                popupmenu.show()
                popupmenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.request_friends_menu_aceptar -> {
                            aceptar_solicitud(uid)
                        }
                        R.id.request_friends_menu_rechazar -> {
                            rechazar_solicitud(uid)
                        }
                    }
                    true
                })
            }
            "Amigos" -> {
                val intent = Intent(this, FriendsActivity::class.java)
                intent.putExtra("UID", uid)
                startActivity(intent)
            }
        }
    }
    private fun setUpTabs() {
        val tabsAdapter = ViewPagerAdapter(supportFragmentManager)
        val timelineFragment = TimelineFragment()
        val likesFragment = LikesFragment()
        val bundle = Bundle()
        bundle.putString("uid",uid)
        timelineFragment.arguments = bundle
        likesFragment.arguments = bundle

        tabsAdapter.addFragment(timelineFragment,"Timeline")
        tabsAdapter.addFragment(likesFragment,"Likes")
        binding.viewPagerTimeline.adapter = tabsAdapter
        binding.tabs.setupWithViewPager(binding.viewPagerTimeline)

    }

    /**
     *
     * Comprueba el uid del perfil que visitamos.
     * @param input
     *
     */
    private fun checkUserProfile() {
        if (!intent.getStringExtra("UserUID").isNullOrEmpty()) {
            uid = intent.getStringExtra("UserUID")!!
        } else {
            uid = auth.currentUser?.uid!!
            binding.layoutImage.setOnClickListener { selectImage(100) } // Cambiar imagen layout
            binding.profileImage.setOnClickListener { selectImage(200) } // Cambiar imagen de perfil
        }
    }

    /**
     *
     * @param input
     *
     */
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

    private fun cancelRequest() {
        var sendRequest = firestore.collection("Friendship").document(currentUserUID).collection("FriendRequest").document(uid)
        var receiveRequest = firestore.collection("Friendship").document(uid).collection("FriendRequest").document(currentUserUID)
        sendRequest.delete().addOnSuccessListener {
            receiveRequest.delete().addOnSuccessListener {
                Toast.makeText(applicationContext,
                    "Has cancelado la solicitud de amistad", Toast.LENGTH_SHORT).show()
                btn_amigos.setText("Añadir")
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Se ha producido un error",Toast.LENGTH_SHORT).show()
        }
    }

    private fun request() {
        //Comprobamos que estamos en el perfil de otro usuario

        var requestReference = firestore.collection("Friendship").document(currentUserUID).collection("FriendRequest").document(uid)
        requestReference.get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val document = task.result
                if(document.exists()){
                    if(document["state"]!!.equals("send")!!){
                        btn_amigos.text = "Solicitud enviada"
                    } else if (document["state"]!!.equals("receive")) {
                        btn_amigos.text = "Responder solicitud"
                    }
                } else {
                    var docFriends = firestore.collection("Friendship").document(currentUserUID).collection("Friends").document(uid)
                    docFriends.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document.exists()) {
                                btn_amigos.text = "Amiga"
                                btn_mensajes.text = "Enviar mensaje"
                            } else{
                                btn_amigos.text = "Añadir"
                            }
                        } else {
                            btn_amigos.text = "Añadir"
                        }
                    }
                }
            } else {
                Log.i("TAG-amigos-perfil-1",btn_amigos.text.toString())
                Toast.makeText(this,"NO HAY SOLICITUD",Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     *
     * @param uid String
     *
     */
    private fun aceptar_solicitud(uid: String) {
        var friendship_reference = firestore.collection("Friendship")
        var friends = FriendRequest(currentUserUID, uid, "friends")

        friendship_reference.document(currentUserUID).collection("Friends").document(uid).set(friends)

        friends = FriendRequest(uid, currentUserUID, "friends")
        friendship_reference.document(uid).collection("Friends")
            .document(currentUserUID).set(friends)
        Toast.makeText(this, "¡Has aceptado la solicitud de amistad!", Toast.LENGTH_SHORT).show()

        friendship_reference.document(currentUserUID).collection("FriendRequest").document(uid).delete()

        friendship_reference.document(uid).collection("FriendRequest")
            .document(currentUserUID).delete()

        btn_amigos.text = "Amiga"
        btn_mensajes.text = "Enviar mensaje"
    }

    /**
     *
     * @param uid String
     *
     */
    private fun rechazar_solicitud(uid: String) {
        var friendship_reference = firestore.collection("Friendship")
        friendship_reference.document(currentUserUID).collection("FriendRequest")
            .document(uid).delete()

        friendship_reference.document(uid).collection("FriendRequest")
            .document(currentUserUID).delete()
        Toast.makeText(this, "Solicitud rechazada.", Toast.LENGTH_SHORT).show()
    }

    /**
     *
     * @param input
     *
     */
    fun sendFriendRequest() {
        // Comprobar si el user actual y el user del perfil visitado son amigos, sino enviar peticion

        var friendRequest_send = FriendRequest(uid,currentUserUID,"send")
        var friendRequest_receive = FriendRequest(uid,currentUserUID,"receive")
        Log.i("sendFriendRequest",friendRequest_send.friend_send_uid)
        Log.i("sendFriendRequest-2",friendRequest_receive.friend_receive_uid)
        firestore.collection("Friendship").document(currentUserUID).collection("FriendRequest")
            .document(uid).set(friendRequest_send)
        firestore.collection("Friendship").document(uid).collection("FriendRequest")
            .document(currentUserUID).set(friendRequest_receive)
    }

    /**
     *
     * @param uid String
     *
     */
    private fun loadPicture(uid: String) {
        // Comprueba si existe imagen de perfil en la bbdd
        profileImage  = binding.profileImage
        layoutImage  = binding.layoutImage

        glidePicture(uid,"perfil",profileImage)
        glidePicture(uid,"layout",layoutImage)
    }

    /**
     *
     * @param uid String
     * @param path String
     * @param image ImageView
     */
    private fun glidePicture(uid: String, path:String, image:ImageView) {
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

    /**
     *
     * @param storageReference StorageReference
     * @param uri Uri
     */
    private fun uploadImage(storageReference: StorageReference, uri: Uri){
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

    /**
     *
     * @param code Int
     *
     */
    private fun selectImage(code: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent,code)
    }

    /**
     *
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            100 -> {
                if(resultCode == Activity.RESULT_OK) {
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
                    binding.layoutImage.setImageURI(result.uri)
                    storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/layout")
                    uploadImage(storageReference,result.uri)
                    code = 0
                } else if((resultCode == Activity.RESULT_OK) && (code==200)) {
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