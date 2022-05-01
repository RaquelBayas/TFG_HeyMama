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
import com.example.heymama.adapters.ForoAdapter
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Consulta
import com.example.heymama.models.Post
import com.example.heymama.models.PostTimeline
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class ConsultasActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consultasArraylist: ArrayList<Consulta>
    private lateinit var idTemasArrayList: ArrayList<String>
    private lateinit var adapter: ConsultaAdapter
    private lateinit var spinnerConsultas: Spinner
    private lateinit var temas: Array<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var rol: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultas)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        getDataUser()

        recyclerView = findViewById(R.id.recyclerView_consultas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        consultasArraylist = arrayListOf()

        spinnerConsultas = findViewById(R.id.spinnerConsultas_prof)
        temas = resources.getStringArray(R.array.temasConsultas)
        val adapter = ArrayAdapter(this,R.layout.spinner_item,temas)
        spinnerConsultas.adapter = adapter

        var selectedItem = spinnerConsultas.selectedItem.toString()
        spinnerConsultas.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, id: Long) {

                if(rol == "Usuario") {
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

    private fun getDataUser(){

        database.reference.child("Usuarios").child(auth.uid.toString()).addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    var user : User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()


                }

                override fun onCancelled(error: DatabaseError) {
                    //TO DO("Not yet implemented")
                }

            })

    }

    private fun getMisConsultas(temaConsulta: String) {
        consultasArraylist.clear()

        firestore.collection("Consultas").document(temaConsulta).collection("Consultas").whereEqualTo("userID",auth.uid.toString())
            .addSnapshotListener{ snapshot, error ->

                Log.i("Misconsultas",snapshot!!.documents.toString())
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        }
                        DocumentChange.Type.MODIFIED -> consultasArraylist.add(dc.document.toObject(
                            Consulta::class.java))
                        DocumentChange.Type.REMOVED -> consultasArraylist.remove(dc.document.toObject(
                            Consulta::class.java))
                    }
                }
                consultasArraylist.sort()
                adapter = ConsultaAdapter(this, consultasArraylist, this)
                adapter.notifyDataSetChanged()

                adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemClicked(position: Int) {
                        Toast.makeText(this@ConsultasActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                        var id_consulta = consultasArraylist[position].id.toString()
                        var tema_consulta = consultasArraylist[position].tema.toString()
                        var id_user_consulta = consultasArraylist[position].userID.toString()
                        var consulta = consultasArraylist[position].consulta.toString()

                        open(id_consulta,tema_consulta,id_user_consulta,consulta)
                    }
                })

                recyclerView.adapter = adapter
                recyclerView.setHasFixedSize(true)

            }

    }
    private fun getConsultas(temaConsulta: String) {
        consultasArraylist.clear()

        firestore = FirebaseFirestore.getInstance()

        firestore.collection("Consultas").document(temaConsulta).collection("Consultas")
            .addSnapshotListener { snapshot, error ->
                for (dc in snapshot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            consultasArraylist.add(dc.document.toObject(Consulta::class.java))
                        }
                        DocumentChange.Type.MODIFIED -> consultasArraylist.add(dc.document.toObject(
                            Consulta::class.java))
                        DocumentChange.Type.REMOVED -> consultasArraylist.remove(dc.document.toObject(
                            Consulta::class.java))
                    }
                }
                consultasArraylist.sort()
                adapter = ConsultaAdapter(this, consultasArraylist, this)
                adapter.notifyDataSetChanged()

                adapter.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                    override fun onItemClicked(position: Int) {
                        Toast.makeText(this@ConsultasActivity,"Item number: $position",Toast.LENGTH_SHORT).show()
                        var id_consulta = consultasArraylist[position].id.toString()
                        var tema_consulta = consultasArraylist[position].tema.toString()
                        var id_user_consulta = consultasArraylist[position].userID.toString()
                        var consulta = consultasArraylist[position].consulta.toString()

                        open(id_consulta,tema_consulta,id_user_consulta,consulta)
                    }
                })

                recyclerView.adapter = adapter
                recyclerView.setHasFixedSize(true)

            }
    }

    /**
     * Método para abrir una nueva actividad en la cual el profesional puede responder la consulta seleccionada.
     *
     * @param id_consulta String
     *
     *
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