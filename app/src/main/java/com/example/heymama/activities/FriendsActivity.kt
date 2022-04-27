package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.adapters.FriendsAdapter
import com.example.heymama.models.FriendRequest
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

class FriendsActivity : AppCompatActivity() {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var recyclerViewFriends: RecyclerView
    private lateinit var friendsArraylist: ArrayList<FriendRequest>
    private lateinit var adapterFriends: FriendsAdapter

    private lateinit var uid: String

    /**
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R .layout.activity_friends)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser

        val intent = intent
        if(intent.getStringExtra("UID") != null) {
            uid = intent.getStringExtra("UID").toString()
        } else {
            uid = auth.currentUser?.uid!!
        }

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        // Recycler View de los amigos
        recyclerViewFriends = findViewById(R.id.recyclerview_amigos)
        recyclerViewFriends.layoutManager = LinearLayoutManager(this)
        recyclerViewFriends.setHasFixedSize(true)
        friendsArraylist = arrayListOf()

        getFriends()

    }

    /**
     *
     *  Método para obtener los amigos agregados.
     *
     * @param input
     *
     */
    private fun getFriends() {
        friendsArraylist.clear()
        var friend = FriendRequest()

        var friendsRef = firestore.collection("Friendship").document(uid).collection("Friends")
        friendsRef.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val docs = value!!.documents
            if (docs.isEmpty()) {
                Toast.makeText(this, "No has agregado a nadie...", Toast.LENGTH_SHORT).show()
            } else {
                friendsRef.get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        friend = document.toObject(FriendRequest::class.java)
                        Log.i("GETFRIENDS", document.data.toString())
                    }
                    friendsArraylist.add(friend)
                    adapterFriends = FriendsAdapter(applicationContext, friendsArraylist, uid)
                    recyclerViewFriends.adapter = adapterFriends
                }
            }
        }
    }

}