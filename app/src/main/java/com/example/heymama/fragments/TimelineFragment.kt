package com.example.heymama.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        postsTLArraylist = arrayListOf()
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        _perfilBinding = ActivityPerfilBinding.inflate(inflater,container,false)

        val data = arguments
        uid = data!!["uid"].toString()
        tabs = _perfilBinding!!.tabs
        recyclerViewTimeline = binding.recyclerViewPerfil
        recyclerViewTimeline.layoutManager = LinearLayoutManager(context)
        recyclerViewTimeline.setHasFixedSize(true)
        adapterPostsTL = PostTimelineAdapter(requireContext().applicationContext,postsTLArraylist,this)
        recyclerViewTimeline.adapter = adapterPostsTL

        if(uid == auth.uid.toString()) {
            loadPostsTL()
        } else {
            checkPublicUser()
        }
        return binding.root
    }

    /**
     * Este método comprueba si un usuario tiene su perfil público o privado.
     * Si es público: se muestran sus posts.
     * Si es privado: se comprueba si pertenece a nuestra lista de amigos.
     */
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
            }
        })
    }

    /**
     * Este método permite comprobar si el usuario pertenece a la lista de amigos:
     * Si pertenece: el tabLayout se muestra y se cargan sus posts.
     * Si no pertenece: el tabLayout no muestra los posts.
     */
    private fun checkFriend() {
        tabs!!.visibility = View.INVISIBLE
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").get()
            .addOnSuccessListener { it ->
                val documents = it.documents
                if(documents.isNotEmpty()) {
                    documents.iterator().forEach(){it ->
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

    /**
     * Este método permite cargar los posts de la timeline.
     */
    private fun loadPostsTL() {
        postsTLArraylist.clear()
        firestore.collection("Timeline").whereEqualTo("userId",uid).addSnapshotListener { snapshots, e ->
            if (e!= null) {
                return@addSnapshotListener
            }
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        postsTLArraylist.add(dc.document.toObject(PostTimeline::class.java))
                    }
                    //DocumentChange.Type.MODIFIED -> postsTLArraylist.add(dc.document.toObject( PostTimeline::class.java))
                    DocumentChange.Type.REMOVED -> postsTLArraylist.remove(dc.document.toObject(
                        PostTimeline::class.java))
                }
            }
            adapterPostsTL.notifyDataSetChanged()
            if(postsTLArraylist.size > 1) {
            postsTLArraylist.sort()}
            Log.i("TIMELINEFRAGMENT",postsTLArraylist.toString())

            adapterPostsTL.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    Log.i("TimelineFragment","Item number: $position")
                }
            })
        }
    }

}