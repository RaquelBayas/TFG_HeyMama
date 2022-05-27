package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityTimelineBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class TimelineActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var recyclerViewTimeline: RecyclerView
    private lateinit var postsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterPostsTL: PostTimelineAdapter
    private lateinit var friendsIds: ArrayList<String>
    private lateinit var edt_post_tl: String
    private lateinit var rol: String
    private lateinit var binding : ActivityTimelineBinding
    /**
     * @constructor
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firebaseStore = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initRecycler()

        binding.btnAddPostTl.setOnClickListener {
            if(!findViewById<EditText>(R.id.edt_post_tl).text.isEmpty()) {
                edt_post_tl = binding.edtPostTl.text.toString()
            }
            add_comment_tl(edt_post_tl,user!!.uid)
            binding.edtPostTl.setText("")
        }
        getUserData()
    }

    private fun initRecycler() {
        recyclerViewTimeline = binding.recyclerViewPosts
        recyclerViewTimeline.layoutManager =  LinearLayoutManager(this)
        friendsIds = arrayListOf()
        postsTLArraylist = arrayListOf()
        adapterPostsTL = PostTimelineAdapter(applicationContext,postsTLArraylist,this)
        recyclerViewTimeline.adapter = adapterPostsTL
        recyclerViewTimeline.setHasFixedSize(false)
        recyclerViewTimeline.recycledViewPool.setMaxRecycledViews(0,0)
    }

    /**
     * Este método obtiene la información del usuario loggeado, concretamente su 'rol'.
     * Dependiendo del rol que tenga, obtendrá los comentarios de la Timeline con el método getCommentsTLAdmin() o getCommentsTL().
     */
    private fun getUserData() {
        var ref = database.reference.child("Usuarios").child(auth.uid.toString()).child("rol")
        ref.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rol = snapshot.value.toString()
                if(rol == "Admin"){
                    getCommentsTLAdmin()
                } else {
                    getCommentsTL()
                }
                binding.swipeRefreshTL.setOnRefreshListener {
                    if(rol == "Admin"){
                        getCommentsTLAdmin()
                    } else {
                        getCommentsTL()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método obtiene los posts de la timeline si el usuario loggeado es el Administrador.
     */
    private fun getCommentsTLAdmin() {
        if(binding.swipeRefreshTL.isRefreshing){
            binding.swipeRefreshTL.isRefreshing = false
        }
        postsTLArraylist.clear()
        firestore.collection("Timeline").addSnapshotListener { snapshots, e ->
            if (e!= null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(PostTimeline::class.java))
                }
            }
            if(postsTLArraylist.size > 1) {
                postsTLArraylist.sort()
            }

            adapterPostsTL.notifyDataSetChanged()
            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    Toast.makeText(this@TimelineActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Este método añade el comentario en la base de datos.
     *
     * @param edt_comment String : Comentario escrito por el usuario.
     * @param uid String : UID del usuario.
     */
    private fun add_comment_tl(edt_comment:String, uid:String) {
        var doctlfb = firestore.collection("Timeline").document()
        var doc_id = doctlfb.id
        val comment = PostTimeline(doc_id,uid,Date(),edt_comment,0,0)
        doctlfb.set(comment)
    }

    /**
     * Este método obtiene los comentarios almacenados en la base de datos.
     */
    private fun getCommentsTL() {
        postsTLArraylist.clear()
        friendsIds.clear()

        if(binding.swipeRefreshTL.isRefreshing){
            binding.swipeRefreshTL.isRefreshing = false
        }

        /**
         * Comprobamos si el usuario tiene amigos:
         *
         */
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").get().addOnSuccessListener { it ->
            val documents = it.documents
            friendsIds.add(auth.uid.toString())
            if(documents.isEmpty()){
                firestore.collection("Usuarios").whereEqualTo("protected",false).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        if(it.id != auth.uid.toString()) {
                        friendsIds.add(it.id)}
                    }
                    friendsIds.iterator().forEach {
                        firestore.collection("Timeline").whereEqualTo("userId",it).addSnapshotListener { snapshots, e ->
                            if (e!= null) {
                                return@addSnapshotListener
                            }
                            for (dc in snapshots!!.documentChanges) {
                                when (dc.type) {
                                    DocumentChange.Type.ADDED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                                    DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(PostTimeline::class.java))
                                }
                            }
                            if(postsTLArraylist.size > 1) {
                                postsTLArraylist.sort()
                            }
                            adapterPostsTL.notifyDataSetChanged()
                            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                                override fun onItemClicked(position: Int) {
                                    Log.i("TimelineActivity","Item number: $position")
                                }
                            })
                        }
                    }
                }
            } else {
                documents.iterator().forEach {
                    if(it["friend_receive_uid"] == auth.uid.toString()) {
                        friendsIds.add(it["friend_send_uid"].toString())
                    } else {
                        friendsIds.add(it["friend_receive_uid"].toString())
                    }
                }
                friendsIds.iterator().forEach {
                    firestore.collection("Timeline").whereEqualTo("userId",it).addSnapshotListener { snapshots, e ->
                        if (e!= null) {
                            return@addSnapshotListener
                        }
                        for (dc in snapshots!!.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                                }
                                DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(
                                    PostTimeline::class.java))
                            }
                        }
                        if(postsTLArraylist.size > 1) {
                            postsTLArraylist.sort()
                        }
                        adapterPostsTL.notifyDataSetChanged()
                        adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                            override fun onItemClicked(position: Int) {
                                Log.i("TimelineActivity","Item number: $position")
                            }
                        })
                    }
                }
            }
            binding.recyclerViewPosts.visibility = View.VISIBLE
        }.addOnFailureListener {
        }
    }

    /**
     * Método para seleccionar los posts, está ligado al Interface, y a PostTimeAdapter.
     *
     * @param position Int : Posición del post en el arraylist.
     */
    override fun onItemClicked(position: Int) {
        val intent = Intent(this, PerfilActivity::class.java)
        if(postsTLArraylist[position].userId.equals(auth.currentUser?.uid)) {
            startActivity(intent)
        } else {
            intent.putExtra("UserUID",postsTLArraylist[position].userId)
            startActivity(intent)
        }
    }
}