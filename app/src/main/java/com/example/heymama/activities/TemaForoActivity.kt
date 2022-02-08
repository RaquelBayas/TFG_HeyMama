package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.CommentsForoAdapter
import com.example.heymama.interfaces.ItemForoListener
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import java.util.*

class TemaForoActivity : AppCompatActivity(), ItemForoListener, Utils {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dbReference: StorageReference
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentsArraylist: ArrayList<Comment>
    private lateinit var adapterComments: CommentsForoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tema_foro)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser

        val intent = intent
        val bundle: Bundle? = intent.extras
        val id_tema: String? = intent.getStringExtra("ID_Tema")
        val title_tema: String? = intent.getStringExtra("Title_Tema")
        val description_tema: String? = intent.getStringExtra("Description_Tema")
        val foroName = intent.getStringExtra("ForoName")

        val txt_sub_tema : TextView = findViewById(R.id.txt_sub_tema)
        txt_sub_tema.text = title_tema
        val txt_foro_description: TextView = findViewById(R.id.txt_foro_descripcion)
        txt_foro_description.text = description_tema

        recyclerViewComments = findViewById(R.id.recyclerView_comments_foro)
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.setHasFixedSize(true)

        commentsArraylist = arrayListOf()

        val button9: Button = findViewById(R.id.button9)
        button9.setOnClickListener {
            showDialog(user!!, foroName.toString(), id_tema!!)
            Log.d("ID TEMA",id_tema)
        }

        getComments(foroName.toString(), id_tema!!)

    }

    override fun onClick(view: Int) {
        when(view) {
            //R.id.btn_home -> finish()//goToActivity(this, RespirarActivity::class.java)
            R.id.button2 -> goToActivity(this, ForosActivity::class.java)
            R.id.button3 -> goToActivity(this, InfoActivity::class.java)
            R.id.button4 -> goToActivity(this, PerfilActivity::class.java)
            //R.id.btn_phone -> goToActivity(this, ContactoActivity::class.java)
        }
    }

    fun showDialog(user: FirebaseUser, foroName: String, urlTema: String) {
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout: View  =inflater.inflate(R.layout.add_comment_foro_layout,null)
        val editText: EditText = dialogLayout.findViewById(R.id.edt_add_comment)
        val textView3: TextView = findViewById(R.id.textView3)
        //val btn_add_comment_foro: Button = findViewById(R.id.btn_add_comment_foro)
        with(builder){
            setTitle("Introduce un comentario")
            setPositiveButton("Añadir") {dialog, which ->
                textView3.text = editText.text.toString()
                add_comment(editText.text.toString(),user,foroName,urlTema)
            }
            setNegativeButton("Cancelar"){dialog, which ->
                Log.d("Cancelar","Negative button selected")
            }

            setView(dialogLayout)
            show()
        }

    }

    fun add_comment(edt_comment:String, user: FirebaseUser, foroName: String, idTema: String) {
        var comment = Comment(edt_comment,user.uid, Date())
        addCommentFB(comment,foroName,idTema)
    }

    // Añade comentarios en Firebase
    fun addCommentFB(comment: Comment, foroName: String, idTema: String) {
        firestore.collection("$idTema/Comentarios").add(comment)
        //firestore.collection("Foros").document("SubForos").collection(foroName).document(idTema).
        //collection("Comentarios").add(comment) //.document("1").set(comment)
    }

    // Obtiene los comentarios de Firebase /
    fun getComments(foroName: String, idTema: String?) {
        firestore = FirebaseFirestore.getInstance()

        //firestore.collection("Foros").document("SubForos").collection(foroName).document(idTema.toString()).collection("Comentarios")
        firestore.collection("$idTema/Comentarios")
            .addSnapshotListener { snapshots, e ->
                if (e!= null) {

                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED ->
                        {
                            commentsArraylist.add(dc.document.toObject(Comment::class.java))
                        }
                        DocumentChange.Type.MODIFIED -> commentsArraylist.add(dc.document.toObject(Comment::class.java))
                        DocumentChange.Type.REMOVED -> commentsArraylist.remove(dc.document.toObject(
                            Comment::class.java))
                    }

                }
                adapterComments = CommentsForoAdapter(this,commentsArraylist,this)

                recyclerViewComments.adapter = adapterComments
            }
    }
}