package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.CommentsPostTLAdapter
import com.example.heymama.databinding.ActivityCommentPostTlactivityBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Notification
import com.example.heymama.models.PostTimeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class CommentPostTLActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var recyclerViewCommentsTimeline: RecyclerView
    private lateinit var commentsPostsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterCommentsPostsTL: CommentsPostTLAdapter

    private lateinit var idpost: String
    private lateinit var iduser: String
    private lateinit var nameuser: String
    private lateinit var textpost: String
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
        nameuser = intent.getStringExtra("name").toString()
        textpost = intent.getStringExtra("comment").toString()

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference //.getReference("Usuarios/$uid/images/perfil")
        firestore = FirebaseFirestore.getInstance()

        initRecycler()
        initPictures()
        getPostTLInfo()

        binding.btnSendPosttl.setOnClickListener{
            add_comment_to_posttl(uid,binding.edtCommentPosttl.text.toString())
        }

        getCommentsPostTL()
        binding.swipeRefreshTLComments.setOnRefreshListener {
            getCommentsPostTL()
        }
    }

    /**
     * Este método carga las imágenes: la del usuario al que pertenece el post en el que comentaremos, y del usuario actual.
     */
    private fun initPictures() {
        storageReference = firebaseStorage.getReference("/Usuarios/$iduser/images/perfil")
        photo_comment_0 = binding.imgCommentPosttl0
        getPictures(photo_comment_0,storageReference)

        storageReference = firebaseStorage.getReference("/Usuarios/"+auth.uid+"/images/perfil")
        photo_comment = binding.imgCommentPosttl1
        getPictures(photo_comment,storageReference)
    }

    /**
     * Este método inicializa el recyclerview, el adapter y el arraylist de los comentarios.
     */
    private fun initRecycler() {
        recyclerViewCommentsTimeline = binding.recyclerCommentsPostTL
        recyclerViewCommentsTimeline.layoutManager = LinearLayoutManager(this)
        recyclerViewCommentsTimeline.setHasFixedSize(true)
        commentsPostsTLArraylist = arrayListOf()
        adapterCommentsPostsTL = CommentsPostTLAdapter(this, idpost, commentsPostsTLArraylist, this)
        recyclerViewCommentsTimeline.adapter = adapterCommentsPostsTL
    }

    /**
     * Este método permite cargar imágenes gracias a la librería Glide
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
     * Este método permite añadir un comentario en un post de la timeline.
     * @param uid String
     * @param edt_comment String
     */
    private fun add_comment_to_posttl(uid:String, edt_comment:String) {
        val doctlfb = firestore.collection("Timeline").document(idpost).collection("Replies").document()
        val doc_id = doctlfb.id

        val comment = PostTimeline(doc_id, uid, Date(), edt_comment,0,0)
        doctlfb.set(comment).addOnSuccessListener {
            val notificationRef = database.reference.child("NotificationsTL").child(iduser)
            val notification = Notification(uid,"",idpost,textpost,"ha comentado en tu post",Date())
            notificationRef.push().setValue(notification)
        }.addOnFailureListener {
            Log.i("CommentPostTLActivity","No se ha podido añadir el comentario.")
        }
    }

    /**
     * Este método permite obtener los comentarios de los posts de la timeline.
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
                    DocumentChange.Type.ADDED -> commentsPostsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    //DocumentChange.Type.MODIFIED -> commentsPostsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    DocumentChange.Type.REMOVED -> commentsPostsTLArraylist.remove(dc.document.toObject(PostTimeline::class.java))
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
     */
    private fun getPostTLInfo() {
        binding.txtNameCommentPosttl.text = nameuser
        binding.txtCommentPosttl.text = textpost
    }
}