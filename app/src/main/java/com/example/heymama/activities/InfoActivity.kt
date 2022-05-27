package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.InfoArticleAdapter
import com.example.heymama.databinding.ActivityInfoBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
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
        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter, y el arraylist de artículos.
     */
    private fun initRecycler() {
        recyclerView = findViewById(R.id.articles_recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.setHasFixedSize(true)
        articlesArraylist = arrayListOf()
        idArticlesArrayList = arrayListOf()
        adapter = InfoArticleAdapter(this,articlesArraylist,this)
        recyclerView.adapter = adapter
    }

    /**
     * Este método permite obtener los artículos publicados.
     */
    private fun getArticlesData() {
        articlesArraylist.clear()
        idArticlesArrayList.clear()
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }

        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Artículos").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.e("InfoActivity", error.toString())
                return@addSnapshotListener
            }
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
            if(articlesArraylist.size>1) {
                articlesArraylist.sort()
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Este método permite implementar la búsqueda de artículos.
     */
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

    /**
     * Este método permite filtrar la búsqueda de artículos.
     */
    private fun filter(articleSearch: String) {
        val articleSearchArrayList = ArrayList<Article>()
        for(article in articlesArraylist) {
            if(article.title!!.lowercase().contains(articleSearch.lowercase()) ) {
                articleSearchArrayList.add(article)
            }
        }
        adapter.filterList(articleSearchArrayList)
    }

    /**
     * Este método permite seleccionar un artículo
     * @param position Int
     */
    override fun onItemClicked(position: Int) {
        val intent = Intent(this, ArticleActivity::class.java)
        intent.putExtra("Rol",rol)
        intent.putExtra("ID_Article", idArticlesArrayList[position])
        intent.putExtra("Title_Article", articlesArraylist[position].title)
        intent.putExtra("Description_Article", articlesArraylist[position].article)
        intent.putExtra("Professional_Article", articlesArraylist[position].professionalID)
        intent.putParcelableArrayListExtra("ArticlesArraylist",articlesArraylist)
        startActivity(intent)
    }

}