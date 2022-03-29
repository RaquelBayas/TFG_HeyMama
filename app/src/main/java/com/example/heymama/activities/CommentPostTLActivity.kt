package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.CommentsPostTLAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.example.heymama.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class CommentPostTLActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var recyclerViewCommentsTimeline: RecyclerView
    private lateinit var commentsPostsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterCommentsPostsTL: CommentsPostTLAdapter

    private lateinit var idpost: String
    private lateinit var iduser: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_post_tlactivity)

        val intent = intent
        idpost = intent.getStringExtra("idpost").toString()
        iduser = intent.getStringExtra("iduser").toString()

        //Log.i("POSTTL",posttl)
        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        var user: FirebaseUser? = auth.currentUser

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/perfil")

        firestore = FirebaseFirestore.getInstance()

        // RECYCLERVIEW TIMELINE
        recyclerViewCommentsTimeline = findViewById(R.id.recyclerCommentsPostTL)
        recyclerViewCommentsTimeline.layoutManager = LinearLayoutManager(this)
        recyclerViewCommentsTimeline.setHasFixedSize(true)
        commentsPostsTLArraylist = arrayListOf()


        storageReference = firebaseStore.getReference("/Usuarios/$iduser/images/perfil")
        val photo_comment_0 : CircleImageView = findViewById(R.id.img_comment_posttl_0)
        getPictures(photo_comment_0,storageReference)

        storageReference = firebaseStore.getReference("/Usuarios/"+auth.uid+"/images/perfil")
        val photo_comment : CircleImageView = findViewById(R.id.img_comment_posttl_1)
        getPictures(photo_comment,storageReference)

        getPostTLInfo(intent)

        val btn_send_posttl: Button = findViewById(R.id.btn_send_posttl)
        btn_send_posttl.setOnClickListener{
            getUserData(dataBaseReference,auth.uid.toString())
            //add_comment_to_posttl(auth.uid.toString(),idpost)
        }

        getCommentsPostTL()
    }

    fun getPictures(image: CircleImageView, storageReference: StorageReference){
        GlideApp.with(applicationContext)
            .load(storageReference)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }

    private fun getUserData(databaseReference: DatabaseReference, uid: String) {
        // REALTIME DATABASE
        val edt_comment : TextView = findViewById(R.id.edt_comment_posttl)

        databaseReference.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value.toString()
                val username = it.child("user").value.toString()
                val bio = it.child("bio").value.toString()
                val rol = "Usuario"
                val email = it.child("Email").value.toString()
                val profilePhoto = "Usuarios/"+uid+"/images/perfil"

                val userdata : User? = User(
                    uid, name, username, email, rol, bio,
                    profilePhoto
                )
                add_comment_to_posttl(uid,edt_comment.text.toString(),userdata!!)
            }
        }

    }

    fun add_comment_to_posttl(uid:String, edt_comment:String, user:User/*iduser:String*/) {

        var doctlfb = firestore.collection("Timeline").document(idpost).collection("Replies").document()
        var doc_id = doctlfb.id

        val comment = PostTimeline(doc_id, uid, Date(), edt_comment,0,0,0)
        doctlfb.set(comment)

        /*
        firestore.collection("Usuarios").document(iduser).addSnapshotListener { value, error ->
            val docs = value?.data
            if (docs!!.isEmpty()) {
                Log.i("Error comment tl","No se ha podido publicar el comentario.")
            }
            val user = User(iduser,docs["Name"].toString(),docs["Username"].toString(),docs["Email"].toString(),docs["Rol"].toString(),docs["Bio"].toString(),
                docs["profilePhoto"].toString())

            var doctlfb = firestore.collection("Timeline").document(idpost).collection("Replies").document()
            var doc_id = doctlfb.id

            val comment = PostTimeline(doc_id, uid, user, Date().toString(), edt_comment.text.toString(),0,0,0)
            doctlfb.set(comment)
            //Toast.makeText(this,"Comentario publicado")
        }

        */
    }

    fun getCommentsPostTL() {
        firestore.collection("Timeline").document(idpost).collection("Replies").addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        commentsPostsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))

                    }
                    DocumentChange.Type.MODIFIED -> commentsPostsTLArraylist.add(
                        dc.document.toObject(
                            PostTimeline::class.java
                        )
                    )
                    DocumentChange.Type.REMOVED -> commentsPostsTLArraylist.remove(
                        dc.document.toObject(
                            PostTimeline::class.java
                        )
                    )
                }
            }
            //commentsPostsTLArraylist.sort()
            adapterCommentsPostsTL = CommentsPostTLAdapter(this, idpost, commentsPostsTLArraylist, this)

            Log.i("COMMENTS_TL: ", "$idpost - $adapterCommentsPostsTL")
            recyclerViewCommentsTimeline.adapter = adapterCommentsPostsTL
        }
    }

    private fun getPostTLInfo(intent: Intent) {
        var name : TextView = findViewById(R.id.txt_name_comment_posttl)
        name.text = intent.getStringExtra("name")
        var comment : TextView = findViewById(R.id.txt_comment_posttl)
        comment.text = intent.getStringExtra("comment")
    }
}