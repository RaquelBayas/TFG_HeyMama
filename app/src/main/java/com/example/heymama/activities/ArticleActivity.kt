package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.models.Article
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ArticleActivity : AppCompatActivity() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser
        firestore = FirebaseFirestore.getInstance()

        var btn_publicar_articulo: Button = findViewById(R.id.btn_publicar_articulo)
        btn_publicar_articulo.setOnClickListener {
            publicar_articulo(user!!)
        }
    }

    fun publicar_articulo(user: FirebaseUser){
        var edt_titulo_articulo : EditText = findViewById(R.id.edt_titulo_articulo)
        var edt_contenido_articulo : EditText = findViewById(R.id.edt_contenido_articulo)

        if(!edt_titulo_articulo.text.isEmpty() && !edt_contenido_articulo.text.isEmpty()) {
            var articulo = Article(edt_titulo_articulo.text.toString(),edt_contenido_articulo.text.toString(),user.uid,
                Date()
            )
            addArticle(articulo, user)
            Toast.makeText(this,"Correcto.", Toast.LENGTH_SHORT).show()
            finish()

        } else {
            Utils.showError(this,"Rellena la información.")
        }

    }

    fun addArticle(articulo: Article, user: FirebaseUser) {
        firestore.collection("Artículos").add(articulo)
    }
}