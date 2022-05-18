package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityPerfilBinding
import com.example.heymama.databinding.ActivityTimelineBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

class TimelineActivity : AppCompatActivity(), ItemRecyclerViewListener {

    // FirebaseAuth object
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

        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        firestore = FirebaseFirestore.getInstance()

        recyclerViewTimeline = binding.recyclerViewPosts

        recyclerViewTimeline.layoutManager =  LinearLayoutManager(this)
        //layoutManager.stackFromEnd = true
        //layoutManager.reverseLayout = true
        friendsIds = arrayListOf()
        postsTLArraylist = arrayListOf()
        adapterPostsTL = PostTimelineAdapter(applicationContext,postsTLArraylist,this)
        recyclerViewTimeline.adapter = adapterPostsTL
        recyclerViewTimeline.setHasFixedSize(false)
        recyclerViewTimeline.recycledViewPool.setMaxRecycledViews(0,0)

        binding.btnAddPostTl.setOnClickListener {
            if(!findViewById<EditText>(R.id.edt_post_tl).text.isEmpty()) {
                edt_post_tl = binding.edtPostTl.text.toString()
            }
            add_comment_tl(edt_post_tl,user!!.uid)
            Toast.makeText(this, "Post add", Toast.LENGTH_SHORT).show()
            binding.edtPostTl.setText("")
        }

        getUserData()
        //getCommentsTL()
    }

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
                TODO("Not yet implemented")
            }
        })
    }

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
                    DocumentChange.Type.ADDED -> {
                        postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    }
                    // DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
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
                    Toast.makeText(this@TimelineActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                }
            })

        }
    }
    /**
     * Este método añade el comentario en la base de datos.
     *
     * @param edt_comment String
     * @param uid String
     *
     */
    private fun add_comment_tl(edt_comment:String, uid:String) {
        var doctlfb = firestore.collection("Timeline").document()
        var doc_id = doctlfb.id
        val comment = PostTimeline(doc_id,uid,/*userdata,*/Date(),edt_comment,0,0)
        doctlfb.set(comment)
    }


    /**
     * Este método obtiene los comentarios de Firebase
     *
     * @param input
     *
     */
    private fun getCommentsTL() {
        postsTLArraylist.clear()
        friendsIds.clear()

        if(binding.swipeRefreshTL.isRefreshing){
            binding.swipeRefreshTL.isRefreshing = false
        }
        //friendsIds.add(auth.uid.toString()) //El usuario verá sus propios posts en la tl

        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").get().addOnSuccessListener { it ->
            val documents = it.documents
            if(documents.isEmpty()){
                friendsIds.add(auth.uid.toString())
            } else {
                friendsIds.add(auth.uid.toString())
                documents.iterator().forEach {
                    if(it["friend_receive_uid"] == auth.uid.toString()) {
                        friendsIds.add(it["friend_send_uid"].toString())
                    } else {
                        friendsIds.add(it["friend_receive_uid"].toString())
                    }
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
                            // DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
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
                            Toast.makeText(this@TimelineActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                        }
                    })
                }

            }
            binding.recyclerViewPosts.visibility = View.VISIBLE
            Log.i("friends-collection",it.documents.toString())
        }.addOnFailureListener {
            Log.i("friends-collection","no existe")
        }

    }

    /**
     * Método para seleccionar los tweets, está ligado al Interface, y a PostTimeAdapter
     *
     * @param position Int
     *
     */
    override fun onItemClicked(position: Int) {
        Toast.makeText(this,"Has seleccionado el tweet # ${position+1}",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PerfilActivity::class.java)
        if(postsTLArraylist[position].userId.equals(auth.currentUser?.uid)) {
            startActivity(intent)
        } else {
            intent.putExtra("UserUID",postsTLArraylist[position].userId)
            startActivity(intent)
        }
    }
}