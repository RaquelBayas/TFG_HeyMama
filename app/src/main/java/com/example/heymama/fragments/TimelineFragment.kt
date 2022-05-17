package com.example.heymama.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityPerfilBinding
import com.example.heymama.databinding.FragmentTimelineBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [TimelineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimelineFragment : Fragment(), ItemRecyclerViewListener {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerViewTimeline: RecyclerView
    private lateinit var postsTLArraylist: ArrayList<PostTimeline>
    private lateinit var adapterPostsTL: PostTimelineAdapter
    private lateinit var uid: String
    private lateinit var tabs: TabLayout
    private var _binding : FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    private var _perfilBinding : ActivityPerfilBinding? = null
    private val perfilBinding get() = _perfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        postsTLArraylist = arrayListOf()
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        _perfilBinding = ActivityPerfilBinding.inflate(inflater,container,false)

        val data = arguments
        uid = data!!["uid"].toString()
        tabs = _perfilBinding!!.tabs

        recyclerViewTimeline = binding.recyclerViewPerfil
        adapterPostsTL = PostTimelineAdapter(requireContext().applicationContext,postsTLArraylist,this)
        recyclerViewTimeline.adapter = adapterPostsTL

        //checkFriend()
        checkPublicUser()

        return binding.root
    }

    private fun checkPublicUser() {
        val ref = database.reference.child("Usuarios").child(uid).child("protected")
        ref.addValueEventListener(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == false) {
                    tabs!!.visibility = View.VISIBLE
                    loadPostsTL()
                } else {
                    checkFriend()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkFriend() {
        tabs!!.visibility = View.INVISIBLE
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").get()
            .addOnSuccessListener {
                val documents = it.documents
                Log.i("checkfriend-0",documents.toString())
                if(documents.isNotEmpty()) {
                    documents.iterator().forEach(){
                        if(it["friend_send_uid"] == uid || it["friend_receive_uid"] == uid) {
                            tabs!!.visibility = View.VISIBLE
                        }
                    }
                } else {
                    tabs!!.visibility = View.INVISIBLE
                }
                if(tabs!!.visibility == View.VISIBLE) {
                    loadPostsTL()
                }
            }
    }

    private fun loadPostsTL() {
        postsTLArraylist.clear()
        var layoutManager = LinearLayoutManager(context)
        recyclerViewTimeline.layoutManager = layoutManager
        recyclerViewTimeline.setHasFixedSize(true)

        firestore.collection("Timeline").whereEqualTo("userId",uid).addSnapshotListener { snapshots, e ->
            if (e!= null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    }
                    DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject(
                        PostTimeline::class.java))
                    DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(
                        PostTimeline::class.java))
                }
            }
            adapterPostsTL.notifyDataSetChanged()
            postsTLArraylist.sort()
            Log.i("adapterTL",adapterPostsTL.toString())
            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    Toast.makeText(context,"Item number: $position", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}