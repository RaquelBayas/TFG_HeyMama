package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.CommentsForoAdapter
import com.example.heymama.databinding.ActivityTemaForoBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.Comment
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class TemaForoActivity : AppCompatActivity(), ItemRecyclerViewListener, Utils {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
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
    private lateinit var userID: String
    private lateinit var rol: String
    private lateinit var uid: String
    private lateinit var time: String
    private lateinit var user: FirebaseUser
    private lateinit var binding: ActivityTemaForoBinding

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemaForoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        user = auth.currentUser!!
        uid = auth.uid.toString()

       initExtras()

        binding.button9.setOnClickListener {
            showDialog(user!!, foroName, id_tema)
        }

        initRecycler()
        getComments(foroName,id)
        getDataUser()

        binding.swipeRefreshTL.setOnRefreshListener {
            getComments(foroName,id)
        }
    }

    private fun initExtras() {
        val intent = intent
        id_tema = intent.getStringExtra("ID_Tema").toString()
        id = intent.getStringExtra("ID").toString()
        userID = intent.getStringExtra("UserID").toString()
        title_tema = intent.getStringExtra("Title_Tema").toString()
        description_tema = intent.getStringExtra("Description_Tema").toString()
        foroName = intent.getStringExtra("ForoName").toString()
        time = intent.getStringExtra("Time").toString()

        var timestamp = time
        var timestamp1 = Timestamp.parse(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")

        binding.txtForoHora.text = dateFormat.format(timestamp1)
        binding.txtSubTema.text = title_tema
        binding.txtForoDescripcion.text = description_tema
    }

    private fun initRecycler() {
        recyclerViewComments = binding.recyclerViewCommentsForo
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.setHasFixedSize(true)
        commentsArraylist = arrayListOf()
    }

    private fun btnMenuForo() {
        btn_menu_foro = findViewById(R.id.btn_menu_foro)
        if((userID == auth.uid.toString()) || (rol == "Admin") ) {
            btn_menu_foro.visibility = View.VISIBLE
            btn_menu_foro.setOnClickListener {
                menuForo()
            }
        }
    }
    /**
     * Obtener el rol del usuario y el nombre
     *
     */

    private fun getDataUser(){
        database.reference.child("Usuarios").child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                rol = user!!.rol.toString()
                binding.txtForoUser.text = user.username

                binding.txtForoUser.setOnClickListener {
                    finish()
                    val intent = Intent(applicationContext,PerfilActivity::class.java)
                    intent.putExtra("userID",user.id)
                    startActivity(intent)
                }
                btnMenuForo()
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
        //btnMenuForo()
    }

    /**
     *
     */
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
                    var reference = firestore.collection("Foros").document("SubForos")
                        .collection(foroName).document(id)
                    reference.delete()
                    reference.collection("Comentarios").addSnapshotListener { value, error ->
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
        val builder = AlertDialog.Builder(this,R.style.CustomForoCommentsLayout).create()
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.add_comment_foro_layout,null)
        val btnPublico : RadioButton = dialogLayout.findViewById(R.id.btn_publico)
        val btnPrivado : RadioButton = dialogLayout.findViewById(R.id.btn_privado)

        val editText: EditText = dialogLayout.findViewById(R.id.edt_add_comment)
        val btn_add_comment: Button = dialogLayout.findViewById(R.id.btn_add_comment_foro)
        builder.setView(dialogLayout)
        btn_add_comment.setOnClickListener {
            var protected : String = ""
            when {
                btnPublico.isChecked -> {
                    protected = btnPublico.text.toString()
                }
                btnPrivado.isChecked -> {
                    protected = btnPrivado.text.toString()
                }
                else -> {
                    Toast.makeText(this,"Selecciona el nivel de privacidad",Toast.LENGTH_SHORT).show()
                }
            }
            if(editText.text.isNotEmpty() && (btnPublico.isChecked || btnPrivado.isChecked)) {
                add_comment(editText.text.toString(),user,foroName,protected,id)
                builder.dismiss()
            } else {
                Toast.makeText(this,"Escribe un comentario",Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    /**
     *
     *
     * @param edt_comment String
     * @param user FirebaseUser
     * @param foroName String
     * @param id String
     */
    private fun add_comment(edt_comment:String, user:FirebaseUser, foroName:String, protected:String, id:String) {
        var comment = Comment(edt_comment,user.uid,protected,Date())
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
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }

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
                commentsArraylist.sort()
                adapterComments = CommentsForoAdapter(this,commentsArraylist,this)

                adapterComments.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemLongClicked(position: Int) {
                        commentsArraylist.removeAt(position)
                        adapterComments.notifyItemRemoved(position)
                    }
                })
                recyclerViewComments.adapter = adapterComments
            }
    }


}