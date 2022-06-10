package app.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.R
import app.example.heymama.adapters.InfoArticleAdapter
import app.example.heymama.databinding.ActivityInfoBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.Article
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
        initFirebase()
        if ((rol == "Profesional") || (rol == "Admin")) {
            btn_add_article = binding.btnAddArticle
            btn_add_article.visibility = View.VISIBLE
            btn_add_article.setOnClickListener {
                val intent = Intent(this,LayoutArticleActivity::class.java)
                intent.putExtra("type","0") //0 PUBLICAR NUEVO ARTÍCULO
                startActivity(intent)
            }
        }
        initRecycler()
        articlesArraylist.clear()
        getArticlesData()
        searchView()

        binding.swipeRefreshTL.setOnRefreshListener {
            getArticlesData()
        }
    }

    /**
     * Este método permite inicializar los objetos de Firebase
     */
    private fun initFirebase() {
        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter, y el arraylist de artículos.
     */
    private fun initRecycler() {
        recyclerView = findViewById(R.id.articles_recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.setHasFixedSize(false)
        articlesArraylist = arrayListOf()
        adapter = InfoArticleAdapter(this,articlesArraylist,this)
        adapter.setHasStableIds(false)
        recyclerView.adapter = adapter
        adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
            override fun onItemClicked(position: Int) {
                val intent = Intent(applicationContext, ArticleActivity::class.java)
                intent.putExtra("Rol",rol)
                intent.putExtra("ID_Article", articlesArraylist[position].idArticle)
                intent.putExtra("Title_Article", articlesArraylist[position].title)
                intent.putExtra("Description_Article", articlesArraylist[position].article)
                intent.putExtra("Professional_Article", articlesArraylist[position].professionalID)
                intent.putParcelableArrayListExtra("ArticlesArraylist",articlesArraylist)
                startActivity(intent)
                Log.i("TimelineActivity","Item number: $position" + "-" + articlesArraylist[position].idArticle)
            }
        })
    }

    /**
     * Este método permite obtener los artículos publicados.
     */
    private fun getArticlesData() {
        articlesArraylist.clear()

        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }

        firestore.collection("Artículos").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.e("InfoActivity", error.toString())
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        if(!articlesArraylist.contains(dc.document.toObject(Article::class.java))) {
                        articlesArraylist.add(dc.document.toObject(Article::class.java))}
                    }
                    DocumentChange.Type.MODIFIED -> {
                        //articlesArraylist.add(dc.document.toObject(Article::class.java))
                        adapter.notifyDataSetChanged()
                    }
                    DocumentChange.Type.REMOVED -> articlesArraylist.remove(dc.document.toObject(Article::class.java))
                }
            }
            Log.i("ARTICLESARRAY",articlesArraylist.toString())
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

    override fun onResume() {
        super.onResume()
        //update whatever your list
        adapter.notifyDataSetChanged()
    }
}