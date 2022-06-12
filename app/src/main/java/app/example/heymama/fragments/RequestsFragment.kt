package app.example.heymama.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.adapters.FriendRequestAdapter
import app.example.heymama.databinding.FragmentRequestsBinding
import app.example.heymama.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RequestsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
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
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)

        initFirebase()
        recyclerViewRequests = binding.recyclerViewRequests
        initRecycler()
        getFriendRequest()
        return binding.root
    }

    /**
     * Este método permite inicializar los objetos de Firebase
     */
    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!
        firestore = FirebaseFirestore.getInstance()
    }

    /**
     * Este método inicializa el recyclerview, el adapter y el arraylist de solicitudes
     */
    private fun initRecycler() {
        requestsArraylist = arrayListOf()
        recyclerViewRequests.layoutManager = LinearLayoutManager(context)
        recyclerViewRequests.setHasFixedSize(true)
        adapterRequests = FriendRequestAdapter(requireContext().applicationContext,requestsArraylist)
        recyclerViewRequests.adapter = adapterRequests
    }

    /**
     * Este método muestra la lista de solicitudes de amistad recibidas.
     */
    private fun getFriendRequest() {
        requestsArraylist.clear()

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