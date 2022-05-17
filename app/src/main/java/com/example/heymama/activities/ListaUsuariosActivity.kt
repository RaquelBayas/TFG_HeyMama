package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.adapters.ListaUsuariosAdapter
import com.example.heymama.databinding.ActivityListaUsuariosBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Post
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class ListaUsuariosActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var user: FirebaseUser
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var listaUsuariosArraylist: ArrayList<User>
    private lateinit var adapter: ListaUsuariosAdapter
    private lateinit var binding: ActivityListaUsuariosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        user = auth.currentUser!!
        uid = auth.uid.toString()

        recyclerView = binding.recyclerViewListaUsuarios
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        listaUsuariosArraylist = arrayListOf()

        getUsuarios()
    }

    private fun getUsuarios() {
        listaUsuariosArraylist.clear()
        var reference = database.reference.child("Usuarios")
        reference.get().addOnSuccessListener {
            var usuario = it.getValue(User::class.java)

            it.children.iterator().forEach { va ->
                var user = va.getValue(User::class.java)
                listaUsuariosArraylist.add(user!!)
                adapter = ListaUsuariosAdapter(applicationContext,listaUsuariosArraylist)
                recyclerView.adapter = adapter
                Log.i("listausers-1",user.toString())
            }
            Log.i("listausers",usuario.toString())
        }
    }

}