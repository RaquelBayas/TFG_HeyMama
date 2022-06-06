package app.example.heymama.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import app.example.heymama.GlideApp
import app.example.heymama.R
import app.example.heymama.Utils
import app.example.heymama.databinding.ActivityPerfilBinding
import app.example.heymama.fragments.LikesFragment
import app.example.heymama.fragments.TimelineFragment
import app.example.heymama.fragments.ViewPagerAdapter
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.FriendRequest
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class PerfilActivity : AppCompatActivity(), ItemRecyclerViewListener {

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
    private lateinit var popupmenu: PopupMenu
    private lateinit var btn_mensajes: Button
    private lateinit var btn_amigos : Button
    private lateinit var profileImage: ImageView
    private lateinit var layoutImage: ImageView
    private lateinit var txt_username_perfil: TextView
    private lateinit var txt_user_perfil: TextView
    private lateinit var txt_user_biografia : TextView


    /**
     * @constructor
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFirebase()
        val intent = intent
        btn_amigos = binding.btnAmigos
        checkUserProfile()

        txt_user_perfil = binding.txtUserPerfil
        txt_username_perfil = binding.txtUsernamePerfil
        txt_user_biografia = binding.txtBiografia
        profileImage = binding.profileImage

        loadPicture(uid)

        var user_ref = firestore.collection("Usuarios").document(uid)
        user_ref.addSnapshotListener { value, error ->
            if(value!!.exists()) {
                val user = value.toObject(User::class.java)
                txt_username_perfil.text = user!!.username.toString()
                txt_user_perfil.text = user.name.toString()
                txt_user_biografia.text = user.bio.toString()
                if (user.rol == "Profesional") {
                    binding.verified.visibility = View.VISIBLE
                }
            }
        }

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
            checkButtonFriends(btn_amigos.text.toString())
        }
        setUpTabs()
    }

    /**
     * Este método permite inicializar los objetos de Firebase
     */
    private fun initFirebase(){
        auth = FirebaseAuth.getInstance()
        currentUserUID = auth.currentUser!!.uid
        dataBase = FirebaseDatabase.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = firebaseStore.reference
    }

    /**
     * Este método permite controlar el estado del botón 'Amigos'
     * @param btn_amigos_text String
     */
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
                alertSendRequest()
            }
            "Responder solicitud" -> {
                menuFriendRequest()
            }
            "Amigos" -> {
                val intent = Intent(this, FriendsActivity::class.java)
                intent.putExtra("UID", uid)
                startActivity(intent)
            }
        }
    }

    /**
     * Este método permite mostrar un alertDialog cuando el usuario desea cancelar una solicitud de amistad enviada a otro usuario.
     */
    private fun alertSendRequest() {
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

    /**
     * Este método permite mostrar un popupMenu con las opciones 'Aceptar solicitud' o 'Rechazar solicitud'.
     */
    private fun menuFriendRequest() {
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

    /**
     * Este método permite agregar fragmentos (TimelineFragment y LikesFragment) en el perfil de cada usuario
     * con la opción de desplazarse entre ambos dentro de la misma activity.
     */
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
     * Comprueba el uid del perfil que visitamos.
     */
    private fun checkUserProfile() {
        if (!intent.getStringExtra("UserUID").isNullOrEmpty()) {
            uid = intent.getStringExtra("UserUID")!!
        } else {
            uid = auth.currentUser?.uid!!
            binding.btnMenuLayout.visibility = View.VISIBLE

            // Cambiar imagen layout
            binding.btnMenuLayout.setOnClickListener {
                if (!Utils.isNetworkAvailable(this)) {
                    Utils.alertDialogInternet(this)
                } else if (Utils.isNetworkAvailable(this)) {
                    popupmenu = PopupMenu(this, binding.btnMenuLayout)
                    popupmenu.menuInflater.inflate(R.menu.menu_imagen, popupmenu.menu)
                    menuImagen(popupmenu, 100)
                }
            }
            //Cambiar imagen perfil
            binding.profileImage.setOnClickListener {
                if (!Utils.isNetworkAvailable(this)) {
                    Utils.alertDialogInternet(this)
                } else if (Utils.isNetworkAvailable(this)) {
                    val popupmenu: PopupMenu = PopupMenu(this, binding.profileImage)
                    popupmenu.menuInflater.inflate(R.menu.menu_imagen, popupmenu.menu)
                    menuImagen(popupmenu, 200)
                }
            }
        }
    }

    /**
     * Este método permite añadir un menú en la imagen de perfil/layout con dos opciones:
     * 1. Ver imagen
     * 2. Cambiar imagen
     * @param popupmenu PopupMenu
     * @param code Int
     */
    private fun menuImagen(popupmenu: PopupMenu, code: Int) {
        var type = "perfil"
        if(code == 100) {
            type = "layout"
        }
        popupmenu.show()
        popupmenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_verImagen -> {
                    val intent = Intent(this,ViewFullImageActivity::class.java)
                    storageReference = FirebaseStorage.getInstance().getReference("/Usuarios/"+uid+"/images/"+type)
                    intent.putExtra("url",storageReference.toString())
                    startActivity(intent)
                }
                R.id.menu_cambiarImagen -> {
                    selectImage(code)
                }
            }
            true
        })
    }

    /**
     * Este método permite cancelar una solicitud de amistad enviada a otro usuario.
     */
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

    /**
     * Este método permite controlar el estado de los botones 'Amigos' y 'Mensajes' los cuales cambiarán dependiendo
     * de si el perfil del usuario que visitamos forma parte de la lista de amigos agregados, se le ha enviado una solicitud
     * de amistad, o no.
     */
    private fun request() {
        //Comprobamos que estamos en el perfil de otro usuario
        var requestReference = firestore.collection("Friendship").document(currentUserUID).collection("FriendRequest").document(uid)
        requestReference.get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                val document = task.result
                if(document.exists()){
                    if((document["state"]!! == "send")!!){
                        btn_amigos.text = "Solicitud enviada"
                    } else if (document["state"]!! == "receive") {
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
                Log.i("PerfilActivity",task.result.toString())
            }
        }
    }

    /**
     * Este método permite al usuario aceptar la solicitud de amistad recibida.
     * @param uid String : UID del usuario que ha enviado la solicitud.
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
     * Este método permite al usuario rechazar la solicitud de amistad enviada por otro.
     * @param uid String : UID del usuario que ha enviado la solicitud.
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
     * Este método permite enviar una solicitud de amistad a otro usuario.
     */
    fun sendFriendRequest() {
        var friendRequest_send = FriendRequest(uid,currentUserUID,"send")
        var friendRequest_receive = FriendRequest(uid,currentUserUID,"receive")

        firestore.collection("Friendship").document(currentUserUID).collection("FriendRequest")
            .document(uid).set(friendRequest_send)
        firestore.collection("Friendship").document(uid).collection("FriendRequest")
            .document(currentUserUID).set(friendRequest_receive)
    }

    /**
     * Este método permite cargar la imagen de perfil y de layout.
     * @param uid String : UID del usuario.
     */
    private fun loadPicture(uid: String) {
        profileImage  = binding.profileImage
        layoutImage  = binding.layoutImage

        glidePicture(uid,"perfil",profileImage)
        glidePicture(uid,"layout",layoutImage)
    }

    /**
     * Este método permite cargar la imagen de perfil y de layout utilizando la librería Glide.
     * @param uid String
     * @param path String
     * @param image ImageView
     */
    private fun glidePicture(uid: String, path:String, image:ImageView) {
        storageReference = firebaseStore.getReference("/Usuarios/"+uid+"/images/"+path)
        GlideApp.with(applicationContext)
            .load(storageReference)
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }

    /**
     * Este método permite añadir la imagen  en la base de datos.
     * @param storageReference StorageReference
     * @param uri Uri
     */
    private fun uploadImage(storageReference: StorageReference, uri: Uri, code: Int){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo foto...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.putFile(uri).addOnSuccessListener {
            if(code == 200) {
                val profilePhoto: Map<String, String> = mapOf("profilePhoto" to storageReference.path)
                dataBase.getReference("Usuarios").child(uid).updateChildren(profilePhoto)
            }
            if(progressDialog.isShowing) progressDialog.dismiss()
        }.addOnFailureListener{
            if(progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(this,"Se ha producido un error",Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Este método permite seleccionar una imagen a partir de la galería de imágenes del dispositivo móvil.
     * @param code Int
     */
    private fun selectImage(code: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent,code)
    }

    /**
     * Este método permite recortar la imagen seleccionada de la galería de imágenes del dispositivo móvil para
     * posteriormente ser subida en la base de datos.
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
                    storageReference = FirebaseStorage.getInstance().getReference("Usuarios/"+auth.currentUser?.uid+"/images/layout")
                    uploadImage(storageReference,result.uri,code)
                    code = 0
                } else if((resultCode == Activity.RESULT_OK) && (code==200)) {
                    binding.profileImage.setImageURI(result.uri)
                    storageReference = FirebaseStorage.getInstance().getReference("Usuarios/"+auth.currentUser?.uid+"/images/perfil")
                    uploadImage(storageReference,result.uri,code)
                    code = 0
                }
                else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.i("PerfilActivity",result.error.toString())
                }
            }
        }
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        app.example.heymama.Utils.updateStatus("offline")
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onStart() {
        super.onStart()
        app.example.heymama.Utils.updateStatus("online")
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        app.example.heymama.Utils.updateStatus("online")
    }
}