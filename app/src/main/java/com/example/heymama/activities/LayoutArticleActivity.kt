package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivityLayoutArticleBinding
import com.example.heymama.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.os.Build
import androidx.annotation.RequiresApi


class LayoutArticleActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    private lateinit var title_article: String
    private lateinit var description_article: String
    private lateinit var id_article: String
    private lateinit var edt_titulo_articulo : EditText
    private lateinit var edt_contenido_articulo : EditText
    private lateinit var type: String
    private lateinit var user: FirebaseUser
    private lateinit var bundle: Bundle
    private lateinit var binding: ActivityLayoutArticleBinding

    /**
     * @param savedInstanceState Bundle
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firestore = FirebaseFirestore.getInstance()

        edt_titulo_articulo = binding.edtTituloArticulo
        edt_contenido_articulo = binding.edtContenidoArticulo

        val intent = intent
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type").toString()
        }

        if (type == "1" && intent.hasExtra("edit_title_article") && intent.hasExtra("edit_description_article") && intent.hasExtra("edit_id_article")) {
            //    publicar_articulo(user!!,1) // 1 EDITAR
            title_article = intent.getStringExtra("edit_title_article")!!.toString()
            description_article = intent.getStringExtra("edit_description_article")!!.toString()
            id_article = intent.getStringExtra("edit_id_article").toString()

            edt_titulo_articulo.setText(title_article)
            edt_contenido_articulo.setText(description_article)
        }

        binding.btnPublicarArticulo.setOnClickListener {
            if(type == "0") {
                publicar_articulo(user!!)
            } else {
                editar_articulo()
                Utils.showToast(this, "Correcto.")
                finish()
            }
        }
    }

    /**
     * Este método permite editar el artículo publicado
     */
    private fun editar_articulo()  {
        val reference_article = firestore.collection("Artículos").document(id_article)
        reference_article.update("article",edt_contenido_articulo.text.toString())
        reference_article.update("title",edt_titulo_articulo.text.toString())
    }

    /**
     * Este método permite publicar un artículo
     * @param user FirebaseUser
     */
    private fun publicar_articulo(user: FirebaseUser){
        val article_ref = firestore.collection("Artículos").document()

        if(!edt_titulo_articulo.text.isEmpty() && !edt_contenido_articulo.text.isEmpty()) {
            var articulo = Article(article_ref.id,
                edt_titulo_articulo.text.toString(),
                edt_contenido_articulo.text.toString(),
                user.uid,
                Date()
            )
            addArticle(articulo, article_ref)
            Utils.showToast(this,"Correcto")
            finish()
        }else {
            Utils.showToast(this,"Rellena la información.")
        }
    }

    /**
     * Este método permite añadir el artículo en la base de datos
     * @param articulo Article
     * @param article_ref DocumentReference
     */
    private fun addArticle(articulo: Article, article_ref: DocumentReference) {
        article_ref.set(articulo)
    }
}