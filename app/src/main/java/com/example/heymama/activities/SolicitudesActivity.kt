package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.heymama.R
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.databinding.ActivitySolicitudesBinding
import com.example.heymama.fragments.*
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


    private lateinit var binding: ActivitySolicitudesBinding
    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usuario
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        setUpTabs()
    }

    private fun setUpTabs() {
        val tabsAdapter = ViewPagerAdapter(supportFragmentManager)
        val solicitudesFragment = RequestsFragment()
        val searchFriends = SearchFriendsFragment()
        val bundle = Bundle()
        bundle.putString("uid",uid)
        solicitudesFragment.arguments = bundle
        searchFriends.arguments = bundle

        tabsAdapter.addFragment(solicitudesFragment,"Solicitudes de amistad")
        tabsAdapter.addFragment(searchFriends,"Buscar amigos")
        binding.viewPagerAmigos.adapter = tabsAdapter
        binding.tabs.setupWithViewPager(binding.viewPagerAmigos)
    }
    /**
     *
     * @param input
     *
     */
}