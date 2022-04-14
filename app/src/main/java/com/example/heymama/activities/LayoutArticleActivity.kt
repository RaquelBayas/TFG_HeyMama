package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.models.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LayoutArticleActivity : AppCompatActivity() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var title_article: String
    private lateinit var description_article: String
    private lateinit var id_article: String
    private lateinit var edt_titulo_articulo : EditText
    private lateinit var edt_contenido_articulo : EditText
    private lateinit var btn_publicar_articulo: Button
    private lateinit var type: String
    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_article)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser
        firestore = FirebaseFirestore.getInstance()

        edt_titulo_articulo = findViewById(R.id.edt_titulo_articulo)
        edt_contenido_articulo = findViewById(R.id.edt_contenido_articulo)


        val intent = intent
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type").toString()
            Log.i("type", intent.getStringExtra("type").toString())
        }

        if (type.equals("1") && intent.hasExtra("edit_title_article") && intent.hasExtra("edit_description_article") && intent.hasExtra("edit_id_article")) {
            //    publicar_articulo(user!!,1) // 1 EDITAR
            title_article = intent.getStringExtra("edit_title_article")!!.toString()
            description_article = intent.getStringExtra("edit_description_article")!!.toString()
            id_article = intent.getStringExtra("edit_id_article").toString()

            edt_titulo_articulo.setText(title_article)
            edt_contenido_articulo.setText(description_article)
        }
        btn_publicar_articulo = findViewById(R.id.btn_publicar_articulo)
        btn_publicar_articulo.setOnClickListener {
            Log.i("type_btn",intent.getStringExtra("type").toString())
            if(type.equals("0")) {

                publicar_articulo(user!!)
            } else {
                editar_articulo()
                Toast.makeText(this, "Correcto.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun editar_articulo()  {
        val reference_article = firestore.collection("Artículos").document(id_article)
        Log.i("reference_art",reference_article.path)
        reference_article.update("article",edt_contenido_articulo.text.toString())
        reference_article.update("title",edt_titulo_articulo.text.toString())

    }

    private fun publicar_articulo(user: FirebaseUser){

        val article_ref = firestore.collection("Artículos").document()

        if(!edt_titulo_articulo.text.isEmpty() && !edt_contenido_articulo.text.isEmpty()) {
            var articulo = Article(article_ref.id,
                edt_titulo_articulo.text.toString(),
                edt_contenido_articulo.text.toString(),
                user.uid,
                Date()
            )
            addArticle(articulo, user, article_ref)
            Toast.makeText(this, "Correcto.", Toast.LENGTH_SHORT).show()
            finish()
        }else {
            Utils.showError(this,"Rellena la información.")
        }
    }

    private fun addArticle(articulo: Article, user: FirebaseUser, article_ref: DocumentReference) {
        article_ref.set(articulo)
    }


}