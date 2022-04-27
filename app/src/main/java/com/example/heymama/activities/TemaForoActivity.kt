package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.CommentsForoAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
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

class TemaForoActivity : AppCompatActivity(), ItemRecyclerViewListener, Utils {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentsArraylist: ArrayList<Comment>
    private lateinit var adapterComments: CommentsForoAdapter

    private lateinit var id_tema: String
    private lateinit var title_tema: String
    private lateinit var description_tema: String
    private lateinit var foroName: String
    private lateinit var btn_menu_foro: Button
    private lateinit var id: String

    /**
     *
     * @param savedInstanceState Bundle
     */
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
        id_tema = intent.getStringExtra("ID_Tema").toString()
        id = intent.getStringExtra("ID").toString()
        title_tema = intent.getStringExtra("Title_Tema").toString()
        description_tema = intent.getStringExtra("Description_Tema").toString()
        foroName = intent.getStringExtra("ForoName").toString()

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
            showDialog(user!!, foroName, id_tema)
        }

        getComments(foroName,id)

        btn_menu_foro = findViewById(R.id.btn_menu_foro)
        btn_menu_foro.visibility = View.VISIBLE
        btn_menu_foro.setOnClickListener {
            menuForo()
        }

    }

    private fun menuForo() {
        val popupMenu: PopupMenu = PopupMenu(this,btn_menu_foro)
        popupMenu.menuInflater.inflate(R.menu.article_menu,popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when(it.itemId) {
                R.id.editar -> {
                    ""
                }
                R.id.eliminar -> {
                    ""
                    firestore.collection("Foros").document("SubForos")
                        .collection(foroName).document(id).delete()
                    firestore.collection("Foros").document("SubForos")
                        .collection(foroName).document(id).collection("Comentarios")
                        .addSnapshotListener { value, error ->
                            val data = value?.documents
                            for(i in data!!.iterator()) {
                                i.reference.delete()
                            }
                            Toast.makeText(this,"Deleted successfully",Toast.LENGTH_SHORT).show()
                        }
                    finish()
                }
            }
            true
        })
    }
    /**
     * Muestra un diálogo que sirve para escribir el comentario que se desea añadir.
     *
     * @param user FirebaseUser
     * @param foroName String
     * @param urlTema String
     *
     */
    private fun showDialog(user: FirebaseUser, foroName: String, urlTema: String) {
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.add_comment_foro_layout,null)
        val editText: EditText = dialogLayout.findViewById(R.id.edt_add_comment)

        with(builder){
            setTitle("Introduce un comentario")
            setPositiveButton("Añadir") {dialog, which ->
                add_comment(editText.text.toString(),user,foroName,urlTema,id)
            }
            setNegativeButton("Cancelar"){dialog, which ->
                Log.d("Cancelar","Negative button selected")
            }
            setView(dialogLayout)
            show()
        }
    }

    /**
     *
     *
     * @param edt_comment String
     * @param user FirebaseUser
     * @param foroName String
     * @param id String
     */
    private fun add_comment(edt_comment:String, user:FirebaseUser, foroName:String, idTema:String, id:String) {
        var comment = Comment(edt_comment,user.uid, Date())
        addCommentFB(comment,foroName,id)
    }

    /**
     * Añade los comentarios en la base de datos
     *
     * @param comment Comment
     * @param foroName String
     * @param idTema String
     * @param id String
     */
     private fun addCommentFB(comment: Comment, foroName: String, id: String) {
        firestore.collection("Foros").document("SubForos").collection(foroName)
            .document(id).collection("Comentarios").add(comment)
    }

    /**
     * Obtiene los comentarios de la base de datos
     *
     * @param foroName String
     *
     */
    private fun getComments(foroName: String, id: String) {
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Foros").document("SubForos").collection(foroName).document(id).collection("Comentarios")
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
                Collections.sort(commentsArraylist)
                adapterComments = CommentsForoAdapter(this,commentsArraylist,this)
                recyclerViewComments.adapter = adapterComments
            }
    }
}