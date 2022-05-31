package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivityPreguntaBinding
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class PreguntaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityPreguntaBinding
    private lateinit var txt_descripcion_foro: EditText
    private lateinit var txt_titulo_foro: EditText

    /**
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreguntaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val foroName = intent.getStringExtra("ForoName")

        dataBase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")
        val user: FirebaseUser = auth.currentUser!!
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        binding.btnEnviar.setOnClickListener {
            enviar_pregunta_foro(user, foroName!!)
        }
    }

    /**
     * Este método permite al usuario agregar un nuevo post (hilo) en el Foro
     * @param user FirebaseUser : Usuario
     * @param foroName String : Nombre del foro
     */
    private fun enviar_pregunta_foro(user:FirebaseUser, foroName: String){
        txt_descripcion_foro = binding.txtDescripcionForo
        txt_titulo_foro = binding.txtTituloForo

        val publico = binding.btnPublico
        val privado = binding.btnPrivado
        var protected = ""
        when {
            publico.isChecked -> {
                protected = publico.text.toString()
            }
            privado.isChecked -> {
                protected = privado.text.toString()
            }
            else -> {
                Utils.showToast(this,"Selecciona el nivel de privacidad")
            }
        }

        val ref = firestore.collection("Foros").document("SubForos").collection(foroName).document()
        val id_ref = ref.id
        if(txt_titulo_foro.text.isNotEmpty() && txt_descripcion_foro.text.isNotEmpty() && (publico.isChecked || privado.isChecked)) {
            val post = Post(id_ref,txt_titulo_foro.text.toString(),txt_descripcion_foro.text.toString(),user.uid,protected, Date())
            addPost(post,ref)
            finish()
        } else {
            Utils.showToast(this,"Rellena la información.")
        }
    }

    /**
     * Este método permie añadir el post (hilo) del Foro en la base de datos.
     * @param post Post
     * @param reference DocumentReference
     */
    private fun addPost(post: Post, reference: DocumentReference) {
        reference.set(post)
    }
}