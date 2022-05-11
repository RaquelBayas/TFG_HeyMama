package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var uid: String

    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var requestsArraylist: ArrayList<FriendRequest>
    private lateinit var adapterRequests: FriendRequestAdapter

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solicitudes)

        // Usuario
        auth = FirebaseAuth.getInstance()
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

    /**
     *
     * @param input
     *
     */
    private fun getFriendRequest() {
        firestore.collection("Friendship").document(auth.currentUser!!.uid).collection("FriendRequest")
            .addSnapshotListener { value, error ->
                if(value != null) {
                    val document = value.documents
                    document.forEach { d ->
                        d.reference.addSnapshotListener { value, error ->
                            if (value != null) {
                                var friendRequest = value.toObject(FriendRequest::class.java)
                                if ((friendRequest != null) && (friendRequest.state == "receive")) { // SÓLO SE MUESTRAN LOS QUE HAN ENVIADO LA SOLICITUD
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