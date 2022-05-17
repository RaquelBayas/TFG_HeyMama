package com.example.heymama.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.adapters.UserAdapter
import com.example.heymama.databinding.FragmentRequestsBinding
import com.example.heymama.databinding.FragmentTimelineBinding
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class RequestsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var database: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var recyclerViewRequests: RecyclerView
    private lateinit var requestsArraylist: ArrayList<FriendRequest>
    private lateinit var adapterRequests: FriendRequestAdapter
    private lateinit var uid: String
    private var _binding : FragmentRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        // Firebase
        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = database.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        recyclerViewRequests = binding.recyclerViewRequests
        requestsArraylist = arrayListOf()
        adapterRequests = FriendRequestAdapter(requireContext().applicationContext,requestsArraylist)
        recyclerViewRequests.adapter = adapterRequests

        getFriendRequest()
        return binding.root
    }

    private fun getFriendRequest() {
        var uidFriend = ""
        requestsArraylist.clear()
        recyclerViewRequests.layoutManager = LinearLayoutManager(context)
        recyclerViewRequests.setHasFixedSize(true)

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
                                    adapterRequests.notifyDataSetChanged()
                                    Log.i("REQUESTFRAGMENT",requestsArraylist[0].toString())
                                }
                            }
                        }
                    }

                    Log.i("adapterRequest",adapterRequests.toString())
                }
            }
    }

}