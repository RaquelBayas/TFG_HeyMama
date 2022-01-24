package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.sql.Timestamp
import java.util.*

class PreguntaActivity : AppCompatActivity() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        val user: FirebaseUser? = auth.currentUser
        // ID en la BBDD
        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference


        var btn_enviar : Button = findViewById(R.id.btn_enviar)
        btn_enviar.setOnClickListener {
            enviar_pregunta_foro(user)
        }
    }

    fun enviar_pregunta_foro(user:FirebaseUser){
        var txt_descripcion_foro : EditText = findViewById(R.id.txt_descripcion_foro)
        var txt_titulo_foro : EditText = findViewById(R.id.txt_titulo_foro)

        if(!txt_titulo_foro.text.isEmpty() && !txt_descripcion_foro.text.isEmpty()) {
            var post = Post(txt_titulo_foro.text.toString(),txt_descripcion_foro.text.toString(),user.uid,
                Date())
            addPost(post)
        } else {
            Utils.showError(this,"Rellena la información.")
        }

    }

    fun addPost(post: Post) {

        firestore.collection("Posts").add(post)

    }
}