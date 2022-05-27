package com.example.heymama.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.adapters.FriendRequestAdapter
import com.example.heymama.databinding.FragmentRequestsBinding
import com.example.heymama.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RequestsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
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

        database = FirebaseDatabase.getInstance()
        dataBaseReference = database.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()

        recyclerViewRequests = binding.recyclerViewRequests
        requestsArraylist = arrayListOf()
        adapterRequests = FriendRequestAdapter(requireContext().applicationContext,requestsArraylist)
        recyclerViewRequests.adapter = adapterRequests

        getFriendRequest()
        return binding.root
    }

    /**
     * Este método muestra la lista de solicitudes de amistad recibidas.
     */
    private fun getFriendRequest() {
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
                                val friendRequest = value.toObject(FriendRequest::class.java)
                                // Sólo se muestran las peticiones de la gente que 'nos' ha enviado una solicitud
                                if ((friendRequest != null) && (friendRequest.state == "receive")) {
                                    requestsArraylist.add(friendRequest)
                                    adapterRequests.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
    }

}