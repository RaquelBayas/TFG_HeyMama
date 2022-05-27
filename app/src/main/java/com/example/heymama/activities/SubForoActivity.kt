package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.databinding.ActivitySubForoBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SubForoActivity : AppCompatActivity(), ItemRecyclerViewListener, com.example.heymama.interfaces.Utils{

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var temasArraylist: ArrayList<Post>
    private lateinit var idTemasArrayList: ArrayList<String>
    private lateinit var adapter: ForoAdapter
    private lateinit var foroName: String
    private lateinit var binding: ActivitySubForoBinding
    /**
     *
     * @constructor
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubForoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        foroName = intent.getStringExtra("ForoName").toString()

        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        initRecycler()
        getTemasData(foroName)

        binding.swipeRefreshTL.setOnRefreshListener {
            getTemasData(foroName)
        }
        binding.btnAddQuestion.setOnClickListener {
            val intent = Intent(this,PreguntaActivity::class.java)
            intent.putExtra("ForoName",foroName)
            startActivity(intent)
        }
        searchView()
    }

    /**
     * Este método inicializa el recyclerview, el adapter y los arraylists
     */
    private fun initRecycler() {
        temasArraylist = arrayListOf()
        idTemasArrayList = arrayListOf()
        adapter = ForoAdapter(temasArraylist,this)
        recyclerView = binding.foroRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    /**
     * Este método permite implementar la búsqueda de foros.
     */
    private fun searchView() {
        binding.searchViewForos.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
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
     * Este método permite filtrar la búsqueda de foros.
     */
    private fun filter(text: String) {
        val postSearchArrayList = ArrayList<Post>()
        for(post in temasArraylist) {
            if(post.title.lowercase().contains(text.lowercase())) {
                postSearchArrayList.add(post)
            }
        }
        adapter.filterList(postSearchArrayList)
    }

    /**
     * Este método sirve para obtener los datos del respectivo tema seleccionado.
     * @param foroName String : Nombre del foro seleccionado.
     */
    private fun getTemasData(foroName: String?) {
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }
        temasArraylist.clear()
        idTemasArrayList.clear()
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Foros").document("SubForos").collection(foroName.toString())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            temasArraylist.add(dc.document.toObject(Post::class.java))
                            idTemasArrayList.add(dc.document.reference.path)
                        }
                        //DocumentChange.Type.MODIFIED -> temasArraylist.add(dc.document.toObject(Post::class.java))
                        DocumentChange.Type.REMOVED -> temasArraylist.remove(dc.document.toObject(Post::class.java))
                    }
                }
                if(temasArraylist.size > 1) {
                temasArraylist.sort()}

                adapter.notifyDataSetChanged()
            }
    }

    /**
     * Este método permite abrir el tema seleccionado.
     * @param position Int
     */
    override fun onItemClicked(position: Int) {
        val intent = Intent(this, TemaForoActivity::class.java)
        intent.putExtra("ID_Tema", idTemasArrayList[position])
        intent.putExtra("ID", temasArraylist[position].id)
        intent.putExtra("UserID",temasArraylist[position].userID)
        intent.putExtra("ForoName",foroName)
        intent.putExtra("Title_Tema", temasArraylist[position].title)
        intent.putExtra("Description_Tema", temasArraylist[position].post)
        intent.putExtra("Time",temasArraylist[position].timestamp!!.toString())
        intent.putExtra("Privacidad",temasArraylist[position].protected)
        startActivity(intent)
    }
}