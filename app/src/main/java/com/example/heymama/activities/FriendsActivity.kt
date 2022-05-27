package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.adapters.UserAdapter
import com.example.heymama.databinding.ActivityFriendsBinding
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

class FriendsActivity : AppCompatActivity() {

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
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        val intent = intent
        if(intent.getStringExtra("UID") != null) {
            uid = intent.getStringExtra("UID").toString()
        } else {
            uid = auth.currentUser?.uid!!
        }

        database = FirebaseDatabase.getInstance()
        dataBaseReference = database.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        initRecycler()
        getFriends()
        searchView()
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter, y el arraylist de amigos.
     */
    private fun initRecycler() {
        recyclerViewFriends = binding.recyclerviewAmigos
        recyclerViewFriends.layoutManager = LinearLayoutManager(this)
        recyclerViewFriends.setHasFixedSize(true)
        friendsArraylist = arrayListOf()
        adapterFriends =  UserAdapter(applicationContext,friendsArraylist,uid)
        recyclerViewFriends.adapter = adapterFriends
    }

    /**
     * Este método permite implementar la búsqueda de amigos.
     */
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
    /**
     * Este método permite filtrar la búsqueda de amigos.
     */
    private fun filter(friendSearch: String) {
        val friendSearchArrayList = ArrayList<User>()
        for(friend in friendsArraylist) {
            if(friend.name!!.lowercase().contains(friendSearch.lowercase())) {
                friendSearchArrayList.add(friend)
            }
        }
        adapterFriends.filterList(friendSearchArrayList)
    }

    /**
     *  Método para obtener los amigos agregados.
     */
    private fun getFriends() {
        friendsArraylist.clear()
        var friend = FriendRequest()
        var uidFriend = ""
        val friendsRef = firestore.collection("Friendship").document(uid).collection("Friends")
        friendsRef.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val docs = value!!.documents
            if (docs.isEmpty()) {
                friendsArraylist.clear()
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
                        friendsArraylist.clear()
                        firestore.collection("Usuarios").document(uidFriend).addSnapshotListener { value, error ->
                            val user = value!!.toObject(User::class.java)
                            if(!friendsArraylist.contains(user)) {
                                friendsArraylist.add(user!!)
                            }
                            adapterFriends.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
}