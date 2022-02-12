package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.example.heymama.R
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.models.Article
import com.example.heymama.models.Post
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class ArticleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firestore: FirebaseFirestore

    lateinit var articlesArraylist: ArrayList<Article>
    lateinit var title_article: String
    lateinit var professional_article: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val intent = intent
        val bundle: Bundle? = intent.extras
        val id_article: String? = intent.getStringExtra("ID_Article")
        title_article = intent.getStringExtra("Title_Article")!!
        val description_article: String? = intent.getStringExtra("Description_Article")
        professional_article = intent.getStringExtra("Professional_Article")!!
        articlesArraylist = intent.getParcelableArrayListExtra<Article>("ArticlesArraylist")!!


        val txt_title_article : TextView = findViewById(R.id.txt_title_article)
        txt_title_article.text = title_article
        val txt_description_article: TextView = findViewById(R.id.txt_description_article)
        txt_description_article.text = description_article


        val rol = intent.getStringExtra("Rol")
        Toast.makeText(this,rol,Toast.LENGTH_SHORT).show()

        if (rol.equals("Profesional")) {
            findViewById<CoordinatorLayout>(R.id.include_article).visibility = View.VISIBLE
            val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
            setSupportActionBar(toolbar)
        }


        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser

    }

    fun deleteArticle(articlesArraylist: ArrayList<Article>) {

        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Artículos").whereEqualTo("title",title_article).whereEqualTo("professionalID",professional_article).get().addOnSuccessListener {
            for (document in it.documents) {
                firestore.collection("Artículos").document(document.id).delete()
            }

        }
        if (!articlesArraylist.isEmpty()) {
            Toast.makeText(this,articlesArraylist.size.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    fun alertDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.eliminar)
            .setMessage(R.string.alert_eliminar)
            .setNegativeButton("Cancelar") { view, _ ->
                Toast.makeText(this, "Cancel button pressed", Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setPositiveButton("Eliminar") { view, _ ->
                deleteArticle(articlesArraylist)
                Toast.makeText(this,"Artículo eliminado",Toast.LENGTH_SHORT).show()
                view.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
    // Menú: editar, eliminar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.article_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editar ->  Toast.makeText(this,"Editar", Toast.LENGTH_SHORT).show()
            R.id.eliminar -> {
                alertDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}