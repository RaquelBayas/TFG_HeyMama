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
import com.example.heymama.databinding.ActivityCommentPostTlactivityBinding
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
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var recyclerViewCommentsTimeline: RecyclerView
    private lateinit var commentsPostsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterCommentsPostsTL: CommentsPostTLAdapter

    private lateinit var idpost: String
    private lateinit var iduser: String
    private lateinit var photo_comment_0: CircleImageView
    private lateinit var photo_comment: CircleImageView

    private lateinit var binding: ActivityCommentPostTlactivityBinding
    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentPostTlactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        idpost = intent.getStringExtra("idpost").toString()
        iduser = intent.getStringExtra("iduser").toString()

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
        adapterCommentsPostsTL = CommentsPostTLAdapter(this, idpost, commentsPostsTLArraylist, this)
        recyclerViewCommentsTimeline.adapter = adapterCommentsPostsTL

        storageReference = firebaseStore.getReference("/Usuarios/$iduser/images/perfil")

        photo_comment_0 = binding.imgCommentPosttl0
        getPictures(photo_comment_0,storageReference)

        storageReference = firebaseStore.getReference("/Usuarios/"+auth.uid+"/images/perfil")

        photo_comment = binding.imgCommentPosttl1
        getPictures(photo_comment,storageReference)

        getPostTLInfo(intent)

        binding.btnSendPosttl.setOnClickListener{
            getUserData(dataBaseReference,auth.uid.toString())
            //add_comment_to_posttl(auth.uid.toString(),idpost)
        }

        getCommentsPostTL()
        binding.swipeRefreshTLComments.setOnRefreshListener {
            getCommentsPostTL()
        }

    }

    /**
     * @param image CircleImageView
     * @param storageReference StorageReference
     */
    private fun getPictures(image: CircleImageView, storageReference: StorageReference){
        GlideApp.with(applicationContext)
            .load(storageReference)
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }

    /**
     * @param databaseReference DatabaseReference
     * @param uid String
     */
    private fun getUserData(databaseReference: DatabaseReference, uid: String) {
        val edt_comment : TextView = findViewById(R.id.edt_comment_posttl)

        databaseReference.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value.toString()
                val username = it.child("user").value.toString()
                val bio = it.child("bio").value.toString()
                val rol = "Usuario"
                val protected = it.child("protected").value
                val status = it.child("status").value.toString()
                val email = it.child("Email").value.toString()
                val profilePhoto = "Usuarios/"+uid+"/images/perfil"

                val userdata : User? = User(
                    uid, name, username, email, rol, protected as Boolean?, bio,status,
                    profilePhoto
                )
                add_comment_to_posttl(uid,edt_comment.text.toString(),userdata!!)
            }
        }
    }

    /**
     * @param uid String
     * @param edt_comment String
     * @param user User
     */
    private fun add_comment_to_posttl(uid:String, edt_comment:String, user:User) {
        var doctlfb = firestore.collection("Timeline").document(idpost).collection("Replies").document()
        var doc_id = doctlfb.id

        val comment = PostTimeline(doc_id, uid, Date(), edt_comment,0,0)
        doctlfb.set(comment).addOnSuccessListener {
            Log.i("CommentPostTLActivity","Comentario añadido.")
        }.addOnFailureListener {
            Log.i("CommentPostTLActivity","No se ha podido añadir el comentario.")
        }
    }

    /**
     * @param input
     */
    fun getCommentsPostTL() {
        if(binding.swipeRefreshTLComments.isRefreshing){
            binding.swipeRefreshTLComments.isRefreshing = false
        }
        commentsPostsTLArraylist.clear()
        firestore.collection("Timeline").document(idpost).collection("Replies").addSnapshotListener { snapshots, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        commentsPostsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    }
                    DocumentChange.Type.MODIFIED -> commentsPostsTLArraylist.add(dc.document.toObject(PostTimeline::class.java)
                    )
                    DocumentChange.Type.REMOVED -> commentsPostsTLArraylist.remove(dc.document.toObject(PostTimeline::class.java)
                    )
                }
            }
            adapterCommentsPostsTL.notifyDataSetChanged()
            if(commentsPostsTLArraylist.size>1) {
                commentsPostsTLArraylist.sort()
            }

        }
    }

    /**
     *
     * @param intent Intent
     *
     */
    private fun getPostTLInfo(intent: Intent) {
        binding.txtNameCommentPosttl.text = intent.getStringExtra("name")
        binding.txtCommentPosttl.text = intent.getStringExtra("comment")
    }
}