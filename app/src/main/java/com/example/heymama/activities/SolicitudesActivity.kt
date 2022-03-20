package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SolicitudesActivity : AppCompatActivity(), ItemRecyclerViewListener {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    lateinit var uid: String

    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var requestsArraylist: ArrayList<FriendRequest>
    private lateinit var adapterRequests: FriendRequestAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser
        uid = auth.currentUser?.uid!!

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference


        // Recycler View de las solicitudes
        recyclerViewRequests = findViewById(R.id.recyclerView_requests)
        recyclerViewRequests.layoutManager = LinearLayoutManager(this)
        recyclerViewRequests.setHasFixedSize(true)
        requestsArraylist = arrayListOf()

        getFriendRequest()

    }

    fun getFriendRequest() {

        firestore.collection("Friendship").document(auth.currentUser!!.uid).collection("FriendRequest")
            .addSnapshotListener { value, error ->
                if(value != null) {
                    val document = value.documents
                    document.forEach { d ->
                        d.reference.addSnapshotListener { value, error ->
                            if (value != null) {
                                var friend_receive_uid = value.get("friend_receive_uid").toString()
                                var friend_send_uid = value.get("friend_send_uid").toString()
                                var state = value.get("state").toString()
                                var friendRequest = FriendRequest(friend_receive_uid,friend_send_uid,state)
                                if ((friendRequest != null) && (state == "receive")) { // SÓLO SE MUESTRAN LOS QUE HAN ENVIADO LA SOLICITUD
                                    requestsArraylist.add(friendRequest)
                                    adapterRequests = FriendRequestAdapter(applicationContext,requestsArraylist)
                                    recyclerViewRequests.adapter = adapterRequests
                                }
                            }
                        }
                    }
                }
            }

    }

}