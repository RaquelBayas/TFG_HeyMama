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
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.databinding.ActivityPerfilBinding
import com.example.heymama.databinding.FragmentLikesBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class LikesFragment : Fragment(), ItemRecyclerViewListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var recyclerViewLikes: RecyclerView
    private lateinit var likesArraylist: ArrayList<PostTimeline>
    private lateinit var adapterLikes: PostTimelineAdapter
    private lateinit var uid: String
    private lateinit var tabs: TabLayout
    private var _binding : FragmentLikesBinding? = null
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
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        _perfilBinding = ActivityPerfilBinding.inflate(inflater,container,false)
        tabs = _perfilBinding!!.tabs
        val data = arguments
        uid = data!!["uid"].toString()
        initRecycler()
        if(uid == auth.uid.toString()) {
            loadPostsTL()
        } else {
            checkFriend()
        }

        return binding.root
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter, y el arraylist de likes.
     */
    private fun initRecycler() {
        recyclerViewLikes = binding.recyclerViewLikes
        val layoutManager = LinearLayoutManager(requireContext().applicationContext)
        recyclerViewLikes.layoutManager = layoutManager
        recyclerViewLikes.setHasFixedSize(true)
        likesArraylist = arrayListOf()
        adapterLikes = PostTimelineAdapter(requireContext().applicationContext,likesArraylist,this)
        recyclerViewLikes.adapter = adapterLikes
    }

    /**
     * Este método permite comprobar si tenemos agregado al usuario:
     * Caso afirmativo: vemos sus likes
     * Caso negativo: si tiene el perfil público podemos comprobar su lista de likes
     */
    private fun checkFriend(){
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").get()
            .addOnSuccessListener { it ->
                val documents = it.documents
                if(documents.isNotEmpty()) {
                    documents.iterator().forEach(){it ->
                        if(it["friend_send_uid"] == uid || it["friend_receive_uid"] == uid) {
                            loadPostsTL()
                        }
                    }
                } else {
                    firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
                        if(value!!.exists()){
                            if(value["protected"] == false) {
                                loadPostsTL()
                            } else {
                                tabs!!.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
    }

    /**
     * Este método permite visualizar los posts del apartado 'Likes' del perfil.
     */
    private fun loadPostsTL() {
        val timelineReference = firestore.collection("Timeline")
        val likesReference = firestore.collection("Likes").document(uid).collection("Likes")
        likesReference.addSnapshotListener { value, error ->
            val docs  = value!!.documents
            if(docs.isEmpty()){
                likesArraylist.clear()
                adapterLikes.notifyDataSetChanged()
            } else {
                likesArraylist.clear()
                docs.iterator().forEach {
                    timelineReference.whereEqualTo("postId",it.id).addSnapshotListener { value, error ->
                        for(doc in value!!.documentChanges) {
                            when(doc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val post = doc.document.toObject(PostTimeline::class.java)
                                    database.reference.child("Usuarios").child(post.userId.toString()).child("protected").get().addOnSuccessListener {
                                        if(it.value == false || uid == post.userId.toString()) {
                                            likesArraylist.add(post)
                                            adapterLikes.notifyDataSetChanged()
                                        } else{
                                            //Se visualizan los posts de los perfiles privados que tenemos agregados.
                                            firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").document(post.userId.toString())
                                                .addSnapshotListener { value, error ->
                                                    if(value!!.exists()) {
                                                        likesArraylist.add(post)
                                                        adapterLikes.notifyDataSetChanged()
                                                    }
                                                }
                                        }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                    likesArraylist.remove(doc.document.toObject(
                                        PostTimeline::class.java))
                                    timelineReference.document(it.id).collection("Likes").whereEqualTo("timestamp",it["timestamp"]).addSnapshotListener { value, error ->
                                        value!!.documents.iterator().forEach {
                                            it.reference.delete()
                                        }
                                    }
                                    likesReference.document(doc.document.id).delete()
                                }
                            }
                        }
                        adapterLikes.notifyDataSetChanged()
                        adapterLikes.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                            override fun onItemClicked(position: Int) {
                                // Toast.makeText(context,"Item number: $position", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
    }
}