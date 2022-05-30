package com.example.heymama.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.adapters.ListaUsuariosAdapter
import com.example.heymama.databinding.FragmentSearchFriendsBinding
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList


class SearchFriendsFragment : Fragment() {

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
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        listaUsuariosArraylist = arrayListOf()
        _binding = FragmentSearchFriendsBinding.inflate(inflater, container, false)
        val data = arguments
        uid = data!!["uid"].toString()

        recyclerView = binding!!.recyclerViewSearchFriends
        adapter = ListaUsuariosAdapter(requireContext().applicationContext,listaUsuariosArraylist)
        recyclerView.adapter = adapter
        searchView()
        searchFriends()
        return binding!!.root
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