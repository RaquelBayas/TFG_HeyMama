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
import com.example.heymama.databinding.FragmentLikesBinding
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
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        val data = arguments
        uid = data!!["uid"].toString()
        initRecycler()
        getLikesPosts()

        return binding.root
    }

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
     *
     */
    private fun getLikesPosts() {
        likesArraylist.clear()
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
                                   likesArraylist.add(doc.document.toObject(PostTimeline::class.java))
                                   Log.i("LikesArray",it.toString())
                               }
                               /*DocumentChange.Type.MODIFIED -> likesArraylist.add(doc.document.toObject(
                                   PostTimeline::class.java))*/
                               DocumentChange.Type.REMOVED -> {
                                   likesArraylist.remove(doc.document.toObject(
                                       PostTimeline::class.java))
                                   timelineReference.document(it.id).collection("Likes").whereEqualTo(
                                       "timestamp",it["timestamp"]).addSnapshotListener { value, error ->
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
                                Toast.makeText(context,"Item number: $position", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
        }
    }

}