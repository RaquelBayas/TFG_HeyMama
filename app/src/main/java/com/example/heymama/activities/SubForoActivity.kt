package com.example.heymama.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.databinding.ActivitySubForoBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Article
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SubForoActivity : AppCompatActivity(), ItemRecyclerViewListener, com.example.heymama.interfaces.Utils{

    // FirebaseAuth object
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
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubForoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        foroName = intent.getStringExtra("ForoName").toString()

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.foro_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        temasArraylist = arrayListOf<Post>()
        idTemasArrayList = arrayListOf()
        getTemasData(foroName)

        binding.swipeRefreshTL.setOnRefreshListener {
            getTemasData(foroName)
        }

        // Add question
        binding.btnAddQuestion.setOnClickListener { view ->
            val intent = Intent(this,PreguntaActivity::class.java)
            intent.putExtra("ForoName",foroName)
            startActivity(intent)
        }

        searchView()
    }

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

    private fun filter(text: String) {
        var postSearchArrayList = ArrayList<Post>()
        for(post in temasArraylist) {
            if(post.title!!.lowercase().contains(text.lowercase())) {
                postSearchArrayList.add(post)
            }
        }
        adapter.filterList(postSearchArrayList)
    }

    /**
     * Este método sirve para obtener los datos del respectivo tema seleccionado.
     *
     * @param foroName String
     *
     */
    private fun getTemasData(foroName: String?) {
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }
        temasArraylist.clear()
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Foros").document("SubForos").collection(foroName.toString())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                //adapter = ForoAdapter(this,temasArraylist,this)
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED ->
                        {
                            temasArraylist.add(dc.document.toObject(Post::class.java))
                            idTemasArrayList.add(dc.document.reference.path)
                        }
                        DocumentChange.Type.MODIFIED -> temasArraylist.add(dc.document.toObject(Post::class.java))
                        DocumentChange.Type.REMOVED -> temasArraylist.remove(dc.document.toObject(Post::class.java))
                    }
                }
                Collections.sort(temasArraylist)
                adapter = ForoAdapter(this,temasArraylist,this)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
    }

    /**
     *
     * @param position Int
     */
    override fun onItemClicked(position: Int) {
        Toast.makeText(this,"Has seleccionado el tema # ${position+1}",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, TemaForoActivity::class.java)
        //intent.putExtra("ForoName",foroName)

        intent.putExtra("ID_Tema", idTemasArrayList[position])
        intent.putExtra("ID", temasArraylist[position].id)
        intent.putExtra("UserID",temasArraylist[position].userID)
        intent.putExtra("ForoName",foroName)
        intent.putExtra("Title_Tema", temasArraylist[position].title)
        intent.putExtra("Description_Tema", temasArraylist[position].post)
        intent.putExtra("Time",temasArraylist[position].timestamp!!.toString())
        Log.i("TimeSUBFORO",temasArraylist[position].timestamp!!.toString())
        startActivity(intent)
    }


}