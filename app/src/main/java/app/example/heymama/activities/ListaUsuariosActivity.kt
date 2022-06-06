package app.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.adapters.ListaUsuariosAdapter
import app.example.heymama.databinding.ActivityListaUsuariosBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        initFirebase()
        initRecycler()
        getUsuarios()
    }

    private fun initFirebase(){
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        user = auth.currentUser!!
        uid = auth.uid.toString()
    }

    private fun initRecycler() {
        listaUsuariosArraylist = arrayListOf()
        recyclerView = binding.recyclerViewListaUsuarios
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter = ListaUsuariosAdapter(applicationContext,listaUsuariosArraylist,this)
        recyclerView.adapter = adapter
    }

    private fun getUsuarios() {
        listaUsuariosArraylist.clear()
        val reference = database.reference.child("Usuarios")
        reference.get().addOnSuccessListener {
            it.children.iterator().forEach { data ->
                val user = data.getValue(User::class.java)
                listaUsuariosArraylist.add(user!!)
            }
            adapter.notifyDataSetChanged()
        }
    }

}