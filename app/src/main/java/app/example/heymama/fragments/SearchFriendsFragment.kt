package app.example.heymama.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.activities.PerfilActivity
import app.example.heymama.adapters.ListaUsuariosAdapter
import app.example.heymama.databinding.FragmentSearchFriendsBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList


class SearchFriendsFragment : Fragment(), ItemRecyclerViewListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var listaUsuariosArraylist: ArrayList<User>
    private lateinit var adapter: ListaUsuariosAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var uid: String
    private var _binding : FragmentSearchFriendsBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        initFirebase()
        _binding = FragmentSearchFriendsBinding.inflate(inflater, container, false)
        val data = arguments
        uid = data!!["uid"].toString()
        initRecycler()
        searchView()
        searchFriends()
        return binding!!.root
    }

    private fun initFirebase() {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    private fun initRecycler() {
        recyclerView = binding!!.recyclerViewSearchFriends
        listaUsuariosArraylist = arrayListOf()
        adapter = ListaUsuariosAdapter(requireContext().applicationContext,listaUsuariosArraylist,this)
        recyclerView.adapter = adapter
        adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
            override fun onItemClicked(position: Int) {
                val intent = Intent(requireContext().applicationContext, PerfilActivity::class.java)
                intent.putExtra("UserUID",listaUsuariosArraylist[position].id)
                startActivity(intent)
            }
        })
    }

    private fun filter(friendSearch: String) {
        val friendSearchArrayList = ArrayList<User>()
        for(friend in listaUsuariosArraylist) {
            if((friend.name!!.lowercase()!!.contains(friendSearch.lowercase())!!) || (friend.username!!.lowercase().contains(friendSearch.lowercase()))) {
                friendSearchArrayList.add(friend)
            }
        }
        adapter.filterList(friendSearchArrayList)
    }

    private fun searchView() {
        binding?.searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filter(p0!!)
                return true
            }
        })
    }

    /**
     * Este método permite mostrar la lista de los usuarios registrados en la aplicación.
     */
    private fun searchFriends(){
        listaUsuariosArraylist.clear()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        val reference = database.reference.child("Usuarios")
        reference.get().addOnSuccessListener {
            listaUsuariosArraylist.clear()
            it.children.iterator().forEach { va ->
                if(va.exists()) {
                    val user = va.getValue(User::class.java)
                    if (user!!.rol != "Admin") {
                        listaUsuariosArraylist.add(user!!)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }

    }

}