package app.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.adapters.ReportsAdapter
import app.example.heymama.databinding.ActivityReportsBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.PostTimeline
import app.example.heymama.models.Report
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ReportsActivity : AppCompatActivity(), ItemRecyclerViewListener {
    private lateinit var binding: ActivityReportsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var reportsArrayList: ArrayList<Report>
    private lateinit var adapterReports: ReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        initRecycler()
        getReports()
    }

    /**
     * Este método permite inicializar los objetos de Firebase
     */
    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    /**
     * Este método permite inicializar el recyclerview, el adapter y el arraylist de denuncias
     */
    private fun initRecycler() {
        recyclerView = binding.recyclerViewReports
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)
        reportsArrayList = arrayListOf()
        adapterReports = ReportsAdapter(this,reportsArrayList)
        recyclerView.adapter = adapterReports
        adapterReports.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener{
            override fun onItemClicked(position: Int) {
                getDataReport(position)
            }
        })
    }

    /**
     * Este método permite obtener la información del post que ha denunciado el usuario
     * y permite abrir el post.
     * Analiza si el post pertenece a la colleción Timeline, o es un comentario realizado en un post de la TL.
     * @param position Int
     */
    private fun getDataReport(position: Int) {
        val report = reportsArrayList[position]
        val intent = Intent(applicationContext, CommentPostTLActivity::class.java)
        firestore.collection("Timeline").document(report.postId).addSnapshotListener { value, error ->
            if(value!!.exists()) {
                val post = value.toObject(PostTimeline::class.java)
                database.reference.child("Usuarios").child(report.userId)
                    .addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val user = snapshot.getValue(User::class.java)
                                intent.putExtra("name", user!!.name)
                                intent.putExtra("comment", post!!.comment.toString())
                                intent.putExtra("idpost", post.postId)
                                intent.putExtra("iduser", post.userId)
                                startActivity(intent)
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            } else {
                firestore.collection("Timeline").addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        it.reference.collection("Replies").whereEqualTo("postId",report.postId).addSnapshotListener { value, error ->
                            if(value!!.documents.isNotEmpty()) {
                                value!!.documents.iterator().forEach {
                                    val post = it.toObject(PostTimeline::class.java)
                                    database.reference.child("Usuarios").child(report.userId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    val user = snapshot.getValue(User::class.java)
                                                    intent.putExtra("name", user!!.name)
                                                    intent.putExtra("comment", post!!.comment.toString())
                                                    intent.putExtra("idpost", post.postId)
                                                    intent.putExtra("iduser", post.userId)
                                                    startActivity(intent)
                                                }
                                            }
                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Este método obtiene las denuncias realizadas por los usuarios
     */
    private fun getReports() {
        reportsArrayList.clear()
        val ref = firestore.collection("PostsReported")
        ref.addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.collection("ReportedBy").addSnapshotListener { value, error ->
                    val documents = value!!.documents
                    if(documents.isEmpty()) {
                        reportsArrayList.clear()
                        adapterReports.notifyDataSetChanged()
                    } else {
                        documents.iterator().forEach {
                            val report = it.toObject(Report::class.java)
                            val ref = firestore.collection("Timeline")
                            ref.document(report!!.postId).get().addOnSuccessListener {
                                if(it.exists()) {
                                    reportsArrayList.add(report!!)
                                } else {
                                    ref.addSnapshotListener { value, error ->
                                        value!!.documents.iterator().forEach {
                                            it.reference.collection("Replies").document(report.postId).get()
                                                .addOnSuccessListener { reportsArrayList.add(report) }}
                                    }
                                }
                            }

                        }
                        adapterReports.notifyDataSetChanged()
                    }
                }
            }
        }
    }
}