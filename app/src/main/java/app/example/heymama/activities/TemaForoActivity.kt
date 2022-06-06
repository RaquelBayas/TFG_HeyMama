package app.example.heymama.activities

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
import app.example.heymama.*
import app.example.heymama.R
import app.example.heymama.adapters.CommentsForoAdapter
import app.example.heymama.databinding.ActivityTemaForoBinding
import app.example.heymama.interfaces.APIService
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.Comment
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class TemaForoActivity : AppCompatActivity(), ItemRecyclerViewListener {

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
    private lateinit var privacidad: String
    private lateinit var user: FirebaseUser
    private lateinit var binding: ActivityTemaForoBinding

    /**
     * @constructor
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemaForoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        uid = auth.uid.toString()

       initExtras()

        binding.button9.setOnClickListener {
            showDialog(user!!, foroName)
        }

        initRecycler()
        getComments(foroName,id)
        getDataUser()
        getDataCurrentUser()
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
        privacidad = intent.getStringExtra("Privacidad").toString()

        val timestamp = time
        val timestamp1 = Timestamp.parse(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")

        binding.txtForoHora.text = dateFormat.format(timestamp1)
        binding.txtSubTema.text = title_tema
        binding.txtForoDescripcion.text = description_tema
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter y el arraylist
     */
    private fun initRecycler() {
        recyclerViewComments = binding.recyclerViewCommentsForo
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.setHasFixedSize(false)
        commentsArraylist = arrayListOf()
        adapterComments = CommentsForoAdapter(commentsArraylist,this)
        recyclerViewComments.adapter = adapterComments
        recyclerViewComments.recycledViewPool.setMaxRecycledViews(0,0)
    }

    /**
     * Este método permite inicializar el menú del foro
     */
    private fun btnMenuForo() {
        btn_menu_foro = findViewById(R.id.btn_menu_foro)
        btn_menu_foro.visibility = View.VISIBLE
        btn_menu_foro.setOnClickListener {
            menuForo()
        }
    }

    /**
     * Obtener el rol del usuario y el nombre
     *
     */
    private fun getDataUser(){
        Log.i("GETDATAUSER",userID)
        database.reference.child("Usuarios").child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                    if (privacidad == "Público") {
                        binding.txtForoUser.text = user.username
                    }
                    binding.txtForoUser.setOnClickListener {
                        val intent = Intent(applicationContext, PerfilActivity::class.java)
                        intent.putExtra("UserUID", userID)
                        startActivity(intent)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getDataCurrentUser() {
        database.reference.child("Usuarios").child(uid).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                    if ((userID == auth.uid.toString()) || (rol == "Admin")) {
                        btnMenuForo()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     *
     */
    private fun menuForo() {
        val popupMenu: PopupMenu = PopupMenu(this,btn_menu_foro)
        popupMenu.menuInflater.inflate(R.menu.post_tl_menu,popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when(it.itemId) {
                R.id.eliminar_post_tl -> {
                    val reference = firestore.collection("Foros").document("SubForos")
                        .collection(foroName).document(id)
                    reference.delete()
                    reference.collection("Comentarios").addSnapshotListener { value, error ->
                        val data = value?.documents
                        for(i in data!!.iterator()) {
                            i.reference.delete()
                        }
                    }
                    finish()
                }
            }
            true
        })
    }
    /**
     * Muestra un diálogo que sirve para escribir el comentario que se desea añadir.
     * @param user FirebaseUser
     * @param foroName String
     * @param urlTema String
     */
    private fun showDialog(user: FirebaseUser, foroName: String) {
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
                    Utils.showToast(this,"Selecciona el nivel de privacidad")
                }
            }
            if(editText.text.isNotEmpty() && (btnPublico.isChecked || btnPrivado.isChecked)) {
                add_comment(editText.text.toString(),user,foroName,protected,id)
                builder.dismiss()
            } else {
                Utils.showToast(this,"Escribe un comentario")
            }
        }
        builder.show()
    }

    /**
     * Este método permite añadir un comentario
     * @param edt_comment String
     * @param user FirebaseUser
     * @param foroName String
     * @param id String
     */
    private fun add_comment(edt_comment:String, user:FirebaseUser, foroName:String, protected:String, id:String) {
        val ref = firestore.collection("Foros").document("SubForos").collection(foroName)
            .document(id).collection("Comentarios").document()
        val comment = Comment(ref.id,edt_comment,user.uid,protected,Date())
        ref.set(comment)
    }

    /**
     * Obtiene los comentarios de la base de datos
     * @param foroName String : Nombre del foro
     * @param id String : ID del comentario
     */
    private fun getComments(foroName: String, id: String) {
        commentsArraylist.clear()
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
                        DocumentChange.Type.ADDED -> commentsArraylist.add(dc.document.toObject(Comment::class.java))
                        //DocumentChange.Type.MODIFIED -> commentsArraylist.add(dc.document.toObject(Comment::class.java))
                        DocumentChange.Type.REMOVED -> commentsArraylist.remove(dc.document.toObject(Comment::class.java))
                    }
                }
                if(commentsArraylist.size>1) {
                commentsArraylist.sort()}
                adapterComments.notifyDataSetChanged()
                adapterComments.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemLongClicked(position: Int) {
                        if(commentsArraylist[position].userID == uid) {
                            deleteComment(commentsArraylist[position])
                        }
                    }
                })
            }
    }

    /**
     * Este método permite eliminar un comentario.
     * @param comment Comment
     */
    private fun deleteComment(comment: Comment) {
        val dialog = AlertDialog.Builder(this,R.style.AlertDialogTheme)
            .setTitle(R.string.eliminar)
            .setMessage("¿Deseas eliminar el comentario?")
            .setNegativeButton("Cancelar") { view, _ ->
                view.dismiss()
            }
            .setPositiveButton("Eliminar") { view, _ ->
                firestore.collection("Foros").document("SubForos").collection(foroName).document(id).collection("Comentarios")
                  .document(comment.id).delete()
                adapterComments.notifyDataSetChanged()
            }
            .create()
        dialog.show()
    }
}