package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.adapters.FriendsAdapter
import com.example.heymama.adapters.UserAdapter
import com.example.heymama.databinding.ActivityFriendsBinding
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
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
    private lateinit var database: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference

    private lateinit var recyclerViewFriends: RecyclerView
    private lateinit var friendsArraylist: ArrayList<User>
    private lateinit var adapterFriends: UserAdapter
    private lateinit var binding: ActivityFriendsBinding
    private lateinit var uid: String
    private lateinit var user: FirebaseUser

    /**
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        val intent = intent
        if (intent.getStringExtra("UID") != null) {
            uid = intent.getStringExtra("UID").toString()
        } else {
            uid = auth.currentUser?.uid!!
        }

        // Firebase
        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = database.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        // Recycler View de los amigos
        recyclerViewFriends = binding.recyclerviewAmigos
        recyclerViewFriends.layoutManager = LinearLayoutManager(this)
        recyclerViewFriends.setHasFixedSize(true)
        friendsArraylist = arrayListOf()
        adapterFriends =  UserAdapter(applicationContext,friendsArraylist,uid)//FriendsAdapter(applicationContext, friendsArraylist, uid)
        recyclerViewFriends.adapter = adapterFriends

        getFriends()

        searchView()
    }

    private fun searchView() {
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filter(p0!!)
                return true
            }
        })
    }

    // friend-send-uid: cuando aceptamos una solicitud
    private fun filter(friendSearch: String) {
        var friendSearchArrayList = ArrayList<User>()
        for(friend in friendsArraylist) {
            if(friend.name!!.toLowerCase()!!.contains(friendSearch.toLowerCase())!!) {
                friendSearchArrayList.add(friend)
            }
        }
        adapterFriends.filterList(friendSearchArrayList)
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
        var uidFriend = ""
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
                        if(friend.friend_send_uid == uid) {
                            uidFriend = friend.friend_receive_uid
                        } else if (friend.friend_receive_uid == uid) {
                            uidFriend = friend.friend_send_uid
                        }
                        getDataUser(uidFriend)
                    }
                    adapterFriends.notifyDataSetChanged()
                }
            }
        }
    }

    private fun getDataUser(uid: String) {
        database.reference.child("Usuarios").child(uid).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                friendsArraylist.add(user!!)
                adapterFriends.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
    }

}