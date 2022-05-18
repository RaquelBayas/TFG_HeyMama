package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.adapters.InfoArticleAdapter
import com.example.heymama.databinding.ActivityInfoBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Article
import com.example.heymama.models.Post
import com.example.heymama.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.util.ArrayList

class InfoActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var articlesArraylist: ArrayList<Article>
    private lateinit var idArticlesArrayList: ArrayList<String>
    private lateinit var adapter: InfoArticleAdapter

    private lateinit var rol: String
    private lateinit var btn_add_article: Button
    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        rol = intent.getStringExtra("Rol")!!

        if ((rol == "Profesional") || (rol == "Admin")) {
            btn_add_article = binding.btnAddArticle
            btn_add_article.visibility = View.VISIBLE
            btn_add_article.setOnClickListener {
                val intent = Intent(this,LayoutArticleActivity::class.java)
                intent.putExtra("type","0") //0 PUBLICAR NUEVO ARTÍCULO
                startActivity(intent)
            }
        }

        initFirebase()
        initRecycler()
        getArticlesData()
        searchView()

        binding.swipeRefreshTL.setOnRefreshListener {
            getArticlesData()
        }
    }

    private fun initFirebase() {
        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
    }

    private fun initRecycler() {
        recyclerView = findViewById(R.id.articles_recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.setHasFixedSize(true)

        articlesArraylist = arrayListOf<Article>()
        idArticlesArrayList = arrayListOf()
        adapter = InfoArticleAdapter(this,articlesArraylist,this)
        recyclerView.adapter = adapter
    }
    /**
     * Este método permite obtener los artículos publicados
     *
     * @param input
     *
     */
    private fun getArticlesData() {
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }

        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Artículos").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("TAG", "listen:error", e)
                return@addSnapshotListener
            }
            //adapter = ForoAdapter(this,temasArraylist,this)
            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        articlesArraylist.add(dc.document.toObject(Article::class.java))
                        idArticlesArrayList.add(dc.document.reference.id)
                    }
                    //DocumentChange.Type.MODIFIED -> articlesArraylist.add(dc.document.toObject(Article::class.java))
                    DocumentChange.Type.REMOVED -> articlesArraylist.remove(dc.document.toObject(Article::class.java))
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchView() {
        binding.searchViewInfo.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                filter(p0!!)
                return true
            }
        })
    }
    private fun filter(articleSearch: String) {
        var articleSearchArrayList = ArrayList<Article>()
        for(article in articlesArraylist) {
            if(article.title!!.lowercase().contains(articleSearch.lowercase())) {
                articleSearchArrayList.add(article)
            }
        }
        adapter.filterList(articleSearchArrayList)
    }
    /**
     * Este método permite eliminar un artículo
     *
     * @param articlessArrayList ArrayList<Article>
     * @param index Int
     *
     */
    private fun deleteArticle(articlessArrayList: ArrayList<Article>, index: Int) {
        if(!articlessArrayList.isEmpty()) {
            articlessArrayList.removeAt(index)
            this.adapter = InfoArticleAdapter(this,articlesArraylist,this)
            recyclerView.adapter = adapter
        }
    }

    /**
     *
     * @param position Int
     *
     */
    override fun onItemClicked(position: Int) {
        Toast.makeText(this,"Has seleccionado el artículo # ${position+1}",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ArticleActivity::class.java)
        //intent.putExtra("ForoName",foroName)
        intent.putExtra("Rol",rol)
        intent.putExtra("ID_Article", idArticlesArrayList.get(position))
        intent.putExtra("Title_Article",articlesArraylist.get(position).title)
        intent.putExtra("Description_Article",articlesArraylist.get(position).article)
        intent.putExtra("Professional_Article",articlesArraylist.get(position).professionalID)
        intent.putParcelableArrayListExtra("ArticlesArraylist",articlesArraylist)
        startActivity(intent)
    }

}