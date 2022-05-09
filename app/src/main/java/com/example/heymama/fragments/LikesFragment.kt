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
import com.example.heymama.databinding.FragmentLikesBinding
import com.example.heymama.databinding.FragmentTimelineBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class LikesFragment : Fragment(), ItemRecyclerViewListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewLikes: RecyclerView
    private lateinit var likesArraylist: ArrayList<PostTimeline>
    private lateinit var adapterLikes: PostTimelineAdapter
    private lateinit var uid: String
    private var _binding : FragmentLikesBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        auth = FirebaseAuth.getInstance()
        likesArraylist = arrayListOf()
        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        val data = arguments
        uid = data!!["uid"].toString()
        recyclerViewLikes = binding.recyclerViewLikes
        adapterLikes = PostTimelineAdapter(requireContext().applicationContext,likesArraylist,this)
        recyclerViewLikes.adapter = adapterLikes

        getLikesPosts()

        return binding.root
    }

    /**
     *
     */
    private fun getLikesPosts() {
        likesArraylist.clear()

        var layoutManager = LinearLayoutManager(requireContext().applicationContext)

        recyclerViewLikes.layoutManager = layoutManager
        recyclerViewLikes.setHasFixedSize(true)

        var timelineReference = firestore.collection("Timeline")
        var likesReference = firestore.collection("Likes").document(uid).collection("Likes")
        likesReference.addSnapshotListener { value, error ->
            var docs  = value!!.documents
            docs.iterator().forEach {
                timelineReference.whereEqualTo("postId",it.id).addSnapshotListener { value, error ->
                    for(doc in value!!.documentChanges) {
                       when(doc.type) {
                           DocumentChange.Type.ADDED -> {
                               likesArraylist.add(doc.document.toObject(PostTimeline::class.java))
                           }
                           DocumentChange.Type.MODIFIED -> likesArraylist.add(doc.document.toObject(
                               PostTimeline::class.java))
                           DocumentChange.Type.REMOVED -> likesArraylist.remove(doc.document.toObject(
                               PostTimeline::class.java))
                       }
                    }
                    adapterLikes.notifyDataSetChanged()
                    adapterLikes.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                        override fun onItemClicked(position: Int) {
                            Toast.makeText(context,"Item number: $position", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }

}