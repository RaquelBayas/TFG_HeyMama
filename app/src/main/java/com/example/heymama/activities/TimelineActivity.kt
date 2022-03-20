package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
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
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

    private lateinit var recyclerViewTimeline: RecyclerView
    private lateinit var postsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterPostsTL: PostTimelineAdapter

    private lateinit var edt_post_tl: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        var user: FirebaseUser? = auth.currentUser

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").getReference("Usuarios/"+auth.currentUser?.uid+"/images/perfil")

        firestore = FirebaseFirestore.getInstance()


        // RECYCLERVIEW TIMELINE
        recyclerViewTimeline = findViewById(R.id.recyclerView_posts)
        //recyclerViewTimeline.layoutManager = LinearLayoutManager(this)
        var layoutManager = LinearLayoutManager(this)
        recyclerViewTimeline.layoutManager = layoutManager
        //layoutManager.stackFromEnd = true
        //layoutManager.reverseLayout = true


        recyclerViewTimeline.setHasFixedSize(true)

        postsTLArraylist = arrayListOf()


        val button9: Button = findViewById(R.id.btn_add_post_tl)
        button9.setOnClickListener {

            if(!findViewById<EditText>(R.id.edt_post_tl).text.isEmpty()) {
                edt_post_tl = findViewById<EditText>(R.id.edt_post_tl).text.toString()
            }
            getUserData(edt_post_tl,dataBaseReference,user!!.uid,)
            Toast.makeText(this, "Post add", Toast.LENGTH_SHORT).show()
        }
        getCommentsTL()

    }


    private fun getUserData(edt_comment:String,databaseReference: DatabaseReference,uid: String) {
        // REALTIME DATABASE
        databaseReference.child(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value.toString()
                val username = it.child("user").value.toString()
                val bio = it.child("bio").value.toString()
                val rol = "Usuario"
                val email = it.child("Email").value.toString()
                val profilePhoto = "Usuarios/"+auth.currentUser?.uid+"/images/perfil"

                val userdata : User? = User(
                    uid, name, username, email, rol, bio,
                    profilePhoto
                )
               add_comment_tl(edt_comment, userdata!!,uid)
            }
        }
        /* FIRESTORE
        val userData= firestore.collection("Usuarios").document(user.uid).get().addOnSuccessListener { document ->

            if (document.exists()) {
                val name = document.data!!.get("name").toString()
                val username = document.data!!.get("username").toString()
                val id = document.data!!.get("id").toString()
                val bio = document.data!!.get("bio").toString()
                val profilePhoto = document.data!!.get("profilePhoto").toString()
                val userdata = User(id,name,username,bio,
                    profilePhoto
                )
                userdata
                this.adapterPostsTL.onCreateViewHolder()
            }

        }*/
        /*val name = userDataRef.get().result.data!!["name"].toString()
        val username = userDataRef.get().result.data!!.get("username").toString()
        val id = userDataRef.get().result.data!!.get("id").toString()
        val bio = userDataRef.get().result.data!!.get("bio").toString()
        val profilePhoto = userDataRef.get().result.data!!.get("profilePhoto").toString()
        val userdata = User(id,name,username,bio,
            profilePhoto
        )*/
    }

    fun add_comment_tl(edt_comment:String, userdata: User, uid:String) {
        var doctlfb = firestore.collection("Timeline").document()
        var doc_id = doctlfb.id
        Log.i("CommentButton0", "position: $doc_id")
        val comment = PostTimeline(doc_id,uid,userdata, Date(),edt_comment,0,0,0)
        doctlfb.set(comment)
    }


    // Obtiene los comentarios de Firebase
    fun getCommentsTL() {
        /*FirebaseFirestore.getInstance().collection("Timeline").orderBy("date",Query.Direction.DESCENDING
            ).get().addOnSuccessListener {
                for(dc in it.documentChanges) {
                    when(dc.type) {
                        DocumentChange.Type.ADDED -> {
                            //val timeline = firestore.collection("Timeline").orderBy("date", Query.Direction.ASCENDING)
                            postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                            //postsTLArraylist.sort()
                        }
                        DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                        DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(
                            PostTimeline::class.java))
                    }
                }
            //postsTLArraylist.sort()
            adapterPostsTL = PostTimelineAdapter(this,postsTLArraylist,this)
            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    Toast.makeText(this@TimelineActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                }
            })
            recyclerViewTimeline.adapter = adapterPostsTL
        }*/
        firestore.collection("Timeline").addSnapshotListener { snapshots, e ->
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
                    Toast.makeText(this@TimelineActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                }
            })
            recyclerViewTimeline.adapter = adapterPostsTL
            recyclerViewTimeline.setHasFixedSize(true)

        }


    }

    // Método para seleccionar los tweets, está ligado al Interface, y a PostTimeAdapter
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