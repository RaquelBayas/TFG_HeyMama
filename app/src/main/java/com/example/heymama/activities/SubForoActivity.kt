package com.example.heymama.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.fragments.SubForoFragment
import com.example.heymama.interfaces.ItemForoListener
import com.example.heymama.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class SubForoActivity : AppCompatActivity(), ItemForoListener, com.example.heymama.interfaces.Utils{

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerView: RecyclerView
    private lateinit var temasArraylist: ArrayList<Post>
    private lateinit var idTemasArrayList: ArrayList<String>
    private lateinit var adapter: ForoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_foro)

        val intent = intent
        val foroName = intent.getStringExtra("ForoName")
        Toast.makeText(this,foroName,Toast.LENGTH_SHORT).show()

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()
        // Usuario
        val user: FirebaseUser? = auth.currentUser

        recyclerView = findViewById(R.id.foro_recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        temasArraylist = arrayListOf<Post>()
        idTemasArrayList = arrayListOf()
        getTemasData(foroName)


        // Add question
        var btn_add_question: Button = findViewById(R.id.btn_add_question)
        btn_add_question.setOnClickListener { view ->
            /*val transaccion = supportFragmentManager.beginTransaction()
            val fragmento = SubForoFragment()

            transaccion.replace(R.id.act_subforo,fragmento)
            //transaccion.addToBackStack(null)
            transaccion.commit()*/
            val intent = Intent(this,PreguntaActivity::class.java)
            intent.putExtra("ForoName",foroName)
            startActivity(intent)
        }
    }


    private fun getTemasData(foroName: String?) {
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Foros").document("SubForos").collection(foroName.toString())
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TAG", "listen:error", e)
                    return@addSnapshotListener
                }
                //adapter = ForoAdapter(this,temasArraylist,this)
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED ->
                        {
                            temasArraylist.add(dc.document.toObject(Post::class.java))
                            idTemasArrayList.add(dc.document.reference.path)
                        }
                        DocumentChange.Type.MODIFIED -> temasArraylist.add(dc.document.toObject(Post::class.java))
                        DocumentChange.Type.REMOVED -> temasArraylist.remove(dc.document.toObject(Post::class.java))
                    }

                }
                adapter = ForoAdapter(this,temasArraylist,this)

                recyclerView.adapter = adapter

            }

    }

    override fun onItemForoClicked(position: Int) {
        Toast.makeText(this,"Has seleccionado el tema # ${position+1}",Toast.LENGTH_SHORT).show()
        val intent = Intent(this, TemaForoActivity::class.java)
        //intent.putExtra("ForoName",foroName)
        intent.putExtra("ID_Tema",idTemasArrayList.get(position))
        intent.putExtra("Title_Tema",temasArraylist.get(position).title)
        intent.putExtra("Description_Tema",temasArraylist.get(position).post)
        startActivity(intent)
    }

    override fun onClick(view: Int) {
        when(view) {
            R.id.btn_home -> goToActivity(this, HomeActivity::class.java)
            R.id.button2 -> goToActivity(this, ForosActivity::class.java)
            R.id.button3 -> goToActivity(this, PerfilActivity::class.java)
            //R.id.btn_add_question -> goToActivity(this, PreguntaActivity::class.java)
        }
    }

    override fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        activity.finish()
    }
}