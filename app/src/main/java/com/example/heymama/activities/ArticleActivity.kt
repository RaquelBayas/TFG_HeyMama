package com.example.heymama.activities

import android.content.Intent
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
    private lateinit var firestore: FirebaseFirestore

    private lateinit var articlesArraylist: ArrayList<Article>
    private lateinit var title_article: String
    private lateinit var id_article: String
    private lateinit var description_article: String
    private lateinit var professional_article: String

    private lateinit var txt_title_article : TextView
    private lateinit var txt_description_article: TextView

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        val intent = intent
        val bundle: Bundle? = intent.extras
        id_article = intent.getStringExtra("ID_Article")!!

        firestore = FirebaseFirestore.getInstance()

        getData()

        title_article = intent.getStringExtra("Title_Article")!!
        description_article = intent.getStringExtra("Description_Article")!!
        professional_article = intent.getStringExtra("Professional_Article")!!
        articlesArraylist = intent.getParcelableArrayListExtra<Article>("ArticlesArraylist")!!


        txt_title_article = findViewById(R.id.txt_title_article)

        txt_description_article = findViewById(R.id.txt_description_article)
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

    /**
     *
     * @param input
     *
     */
    private fun getData() {
        firestore.collection("Artículos").document(id_article).addSnapshotListener { value, error ->
            if(error!=null)  {
                Log.w("TAG", "listen:error", error)
                return@addSnapshotListener
            }
            var data = value!!.data
            txt_title_article.text = data!!["title"].toString()
            txt_description_article.text = data["article"].toString()
        }
    }

    /**
     *
     * @param firestore FirebaseFirestore
     * @param articlesArraylist ArrayList<Article>
     *
     */
    private fun deleteArticle(firestore: FirebaseFirestore, articlesArraylist: ArrayList<Article>) {
        firestore.collection("Artículos").whereEqualTo("title",title_article).whereEqualTo("professionalID",professional_article).get().addOnSuccessListener {
            for (document in it.documents) {
                firestore.collection("Artículos").document(document.id).delete()
            }
        }
        if (articlesArraylist.isNotEmpty()) {
            Toast.makeText(this,articlesArraylist.size.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *
     * @param input
     *
     */
    private fun delete_alertDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.eliminar)
            .setMessage(R.string.alert_eliminar)
            .setNegativeButton("Cancelar") { view, _ ->
                Toast.makeText(this, "Cancel button pressed", Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setPositiveButton("Eliminar") { view, _ ->
                deleteArticle(firestore,articlesArraylist)
                Toast.makeText(this,"Artículo eliminado",Toast.LENGTH_SHORT).show()
                view.dismiss()
                finish()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }
    // Menú: editar, eliminar
    /**
     *
     * @param menu Menu
     *
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.article_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     *
     * @param item MenuItem
     *
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.editar -> {
                val intent = Intent(this,LayoutArticleActivity::class.java)
                intent.putExtra("type","1") // 1 EDITAR ARTÍCULO
                intent.putExtra("edit_title_article",title_article)
                intent.putExtra("edit_description_article",description_article)
                intent.putExtra("edit_id_article",id_article)
                Toast.makeText(this, "Editar", Toast.LENGTH_SHORT).show()
                this.startActivity(intent)
            }
            R.id.eliminar -> {
                delete_alertDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}