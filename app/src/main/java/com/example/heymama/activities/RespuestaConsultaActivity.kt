package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.RespuestaConsultaAdapter
import com.example.heymama.databinding.ActivityRespuestaConsultaBinding
import com.example.heymama.models.Consulta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RespuestaConsultaActivity : AppCompatActivity() {

    private lateinit var id_consulta : String
    private lateinit var tema_consulta : String
    private lateinit var id_user_consulta : String
    private lateinit var consulta : String

    private lateinit var txt_consulta_post_respuesta: TextView
    private lateinit var txt_consulta_user_respuesta: TextView

    private lateinit var txt_respuesta_consulta: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: RecyclerView
    private lateinit var consultasArraylist: ArrayList<Consulta>
    private lateinit var respuestasArraylist: ArrayList<Consulta>
    private lateinit var respuestaConsultaAdapter: RespuestaConsultaAdapter

    private lateinit var binding: ActivityRespuestaConsultaBinding
    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRespuestaConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = binding.recyclerViewConsultasRespuestas
        consultasArraylist = arrayListOf()
        respuestasArraylist = arrayListOf()
        //respuestaConsultaAdapter = RespuestaConsultaAdapter(this,consultasArraylist,respuestasArraylist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val intent = intent
        if (intent.hasExtra("id_consulta") && intent.hasExtra("tema_consulta") && intent.hasExtra("id_user_consulta") && intent.hasExtra("consulta")) {
            id_consulta = intent.getStringExtra("id_consulta").toString()
            tema_consulta = intent.getStringExtra("tema_consulta").toString()
            id_user_consulta = intent.getStringExtra("id_user_consulta").toString()
            consulta = intent.getStringExtra("consulta").toString()
        }


        txt_respuesta_consulta = binding.txtRespuestaConsulta
        var btn_send_respuesta_consulta : ImageView = binding.btnSendRespuestaConsulta
        btn_send_respuesta_consulta.setOnClickListener {
            if(txt_respuesta_consulta.text.isEmpty()) {
                Toast.makeText(this,"Introduce una respuesta",Toast.LENGTH_SHORT).show()
            } else {
                sendReply()
            }
        }
        showConsultaRespuestas()
    }

    private fun showConsultaRespuestas() {
        consultasArraylist.clear()
        respuestasArraylist.clear()
        firestore.collection("Consultas").document(tema_consulta).collection("Consultas")
            .document(id_consulta).addSnapshotListener { value, error ->
                var consulta = value!!.data!!["consulta"]
                var consulta2 = value!!.toObject(Consulta::class.java)
                consultasArraylist.add(consulta2!!)
                Log.i("consultarespuesta",consulta2.toString())
                respuestaConsultaAdapter = RespuestaConsultaAdapter(this,consultasArraylist)
                respuestaConsultaAdapter.notifyDataSetChanged()
                recyclerView.adapter = respuestaConsultaAdapter
            }

        firestore.collection("Consultas").document(tema_consulta).collection("Consultas").document(id_consulta).collection("Respuestas")
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
                    Log.i("respuestas",dc.document.toString())
                }

                respuestaConsultaAdapter = RespuestaConsultaAdapter(this,consultasArraylist)
                respuestaConsultaAdapter.notifyDataSetChanged()
                recyclerView.adapter = respuestaConsultaAdapter
                //Log.i("resp-array",respuestaConsultaAdapter.toString())
            }



        //respuestaConsultaAdapter.notifyDataSetChanged()


    }

    private fun sendReply() {

        var ref = firestore.collection("Consultas").document(tema_consulta).collection("Consultas").document(id_consulta)
            .collection("Respuestas").document()

        var respuesta = Consulta(ref.id,auth.uid.toString(),tema_consulta,txt_respuesta_consulta.text.toString(), Date())

        try {
            ref.set(respuesta)
            Toast.makeText(this,"Respuesta enviada correctamente",Toast.LENGTH_SHORT).show()
            txt_respuesta_consulta .setText("")
        } catch(e: Exception) {
            print(e.message)
        }

    }



}