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
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.adapters.InfoArticleAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Article
import com.example.heymama.models.Post
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

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var articlesArraylist: ArrayList<Article>
    private lateinit var idArticlesArrayList: ArrayList<String>
    private lateinit var adapter: InfoArticleAdapter

    private lateinit var rol: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val intent = intent
        rol = intent.getStringExtra("Rol")!!
        Toast.makeText(this,rol,Toast.LENGTH_SHORT).show()

        if (rol.equals("Profesional")) {
            val btn_add_article : Button = findViewById(R.id.btn_add_article)
            btn_add_article.visibility = View.VISIBLE
        }
        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser

        recyclerView = findViewById(R.id.articles_recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.setHasFixedSize(true)

        articlesArraylist = arrayListOf<Article>()
        idArticlesArrayList = arrayListOf()
        getArticlesData()


        // Barra de navegación inferior
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_home -> finish()
                R.id.nav_bottom_item_respirar -> goToActivity(this,RespirarActivity::class.java)
            }
        }

        var btn_add_article : Button = findViewById(R.id.btn_add_article)
        btn_add_article.setOnClickListener {
            goToActivity(this,LayoutArticleActivity::class.java)
        }
    }

    private fun getArticlesData() {
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Artículos")
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
                            articlesArraylist.add(dc.document.toObject(Article::class.java))
                            idArticlesArrayList.add(dc.document.reference.path)
                        }
                        DocumentChange.Type.MODIFIED -> articlesArraylist.add(dc.document.toObject(Article::class.java))
                        DocumentChange.Type.REMOVED -> articlesArraylist.remove(dc.document.toObject(Article::class.java))
                    }

                }
                adapter = InfoArticleAdapter(this,articlesArraylist,this)

                recyclerView.adapter = adapter

            }

    }

    fun deleteArticle(articlessArrayList: ArrayList<Article>, index: Int) {
        if(!articlessArrayList.isEmpty()) {
            articlessArrayList.removeAt(index)
            this.adapter = InfoArticleAdapter(this,articlesArraylist,this)
            recyclerView.adapter = adapter
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
    }

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