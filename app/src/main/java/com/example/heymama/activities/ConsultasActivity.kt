package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ConsultaAdapter
import com.example.heymama.databinding.ActivityConsultasBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Consulta
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class ConsultasActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consultasArraylist: ArrayList<Consulta>
    private lateinit var adapter: ConsultaAdapter
    private lateinit var spinnerConsultas: Spinner
    private lateinit var temas: Array<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var rol: String
    private lateinit var binding: ActivityConsultasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        getDataUser()
        initRecycler()

        spinnerConsultas = binding.spinnerConsultasProf
        temas = resources.getStringArray(R.array.temasConsultas)
        val adapter = ArrayAdapter(this,R.layout.spinner_item,temas)
        spinnerConsultas.adapter = adapter
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
     * Este método permite inicializar el recyclerview, el adapter y el arraylist de consultas
     */
    private fun initRecycler() {
        recyclerView = binding.recyclerViewConsultas
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        consultasArraylist = arrayListOf()
        adapter = ConsultaAdapter(this, consultasArraylist, this)
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
    }

    /**
     * Este método permite obtener la información del usuario: concretamente el 'rol'
     */
    private fun getDataUser(){
        database.reference.child("Usuarios").child(auth.uid.toString()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                    getSpinner()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método permite mirar la lista de consultas específicas de un tema
     */
    private fun getSpinner() {
        spinnerConsultas.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (rol == "Usuario") {
                    getMisConsultas(temas[position])
                } else {
                    getConsultas(temas[position])
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                if(rol == "Usuario") {
                    getMisConsultas("Embarazo")
                } else {
                    getConsultas("Embarazo")  // Tema por defecto, es el primer tema del spinner
                }
            }
        }
    }

    /**
     * Este método permite al propio usuario revisar las consultas que ha realizado a los profesionales.
     * @param temaConsulta String : Tema de la consulta seleccionado.
     */
    private fun getMisConsultas(temaConsulta: String) {
        consultasArraylist.clear()

        firestore.collection("Consultas").document(temaConsulta).collection("Consultas").whereEqualTo("userID",auth.uid.toString())
            .addSnapshotListener{ snapshot, error ->
                if (error != null) {
                    Log.e("ConsultasActivity",error.toString())
                    return@addSnapshotListener
                }
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        //DocumentChange.Type.MODIFIED -> consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        DocumentChange.Type.REMOVED -> consultasArraylist.remove(dc.document.toObject(Consulta::class.java))
                    }
                }
                consultasArraylist.sort()
                adapter = ConsultaAdapter(this, consultasArraylist, this)
                adapter.setHasStableIds(true)
                adapter.notifyDataSetChanged()

                adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemClicked(position: Int) {
                        val id_consulta = consultasArraylist[position].id.toString()
                        val tema_consulta = consultasArraylist[position].tema.toString()
                        val id_user_consulta = consultasArraylist[position].userID.toString()
                        val consulta = consultasArraylist[position].consulta.toString()
                        open(id_consulta,tema_consulta,id_user_consulta,consulta)
                    }
                })
                recyclerView.adapter = adapter
                recyclerView.setHasFixedSize(true)
            }
    }

    /**
     * Este método permite obtener las consultas realizadas por un usuario dependiendo del tema seleccionado.
     * @param temaConsulta String : Tema seleccionado.
     */
    private fun getConsultas(temaConsulta: String) {
        consultasArraylist.clear()
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Consultas").document(temaConsulta).collection("Consultas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConsultasActivity",error.toString())
                    return@addSnapshotListener
                }
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        //DocumentChange.Type.MODIFIED -> consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        DocumentChange.Type.REMOVED -> consultasArraylist.remove(dc.document.toObject(Consulta::class.java))
                    }
                }
                if(consultasArraylist.size > 1) {
                    consultasArraylist.sort()
                }
                adapter = ConsultaAdapter(this, consultasArraylist, this)
                adapter.setHasStableIds(true)
                adapter.notifyDataSetChanged()

                adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemClicked(position: Int) {
                        Toast.makeText(this@ConsultasActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                        val id_consulta = consultasArraylist[position].id.toString()
                        val tema_consulta = consultasArraylist[position].tema.toString()
                        val id_user_consulta = consultasArraylist[position].userID.toString()
                        val consulta = consultasArraylist[position].consulta.toString()
                        open(id_consulta,tema_consulta,id_user_consulta,consulta)
                    }
                })
                recyclerView.adapter = adapter
                recyclerView.setHasFixedSize(true)
            }
    }

    /**
     * Método para abrir una nueva actividad en la cual el profesional puede responder la consulta seleccionada.
     * @param id_consulta String : ID de la consulta.
     * @param tema_consulta String : Tema de la consulta.
     * @param id_user_consulta String : ID del usuario que ha realizado la consulta.
     * @param consulta String : Texto de la consulta.
     */
    private fun open(id_consulta: String,tema_consulta: String,id_user_consulta: String, consulta: String) {
        val intent = Intent(this, RespuestaConsultaActivity::class.java)
        intent.putExtra("id_consulta",id_consulta)
        intent.putExtra("tema_consulta",tema_consulta)
        intent.putExtra("id_user_consulta",id_user_consulta)
        intent.putExtra("consulta",consulta)
        startActivity(intent)
    }
}