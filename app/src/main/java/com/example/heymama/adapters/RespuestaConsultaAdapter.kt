package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.models.Consulta
import com.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class RespuestaConsultaAdapter(private val context: Context, private val consultaArrayList: ArrayList<Consulta>) : RecyclerView.Adapter<RespuestaConsultaAdapter.HolderConsulta>() {
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firestore: FirebaseFirestore

    private val MESSAGE_CONSULTA = 0
    private val MESSAGE_RESPUESTA = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RespuestaConsultaAdapter.HolderConsulta {
        return if (viewType == MESSAGE_CONSULTA) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.consulta,parent,false)
            HolderConsulta(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.consulta_respuesta,parent,false)
            HolderConsulta(view)
           // respuesta = view.findViewById(R.id.txt_consulta_respuesta_post) //= findViewById(R.id.txt_consulta_respuesta_post)
        }
    }

    override fun onBindViewHolder(holder: RespuestaConsultaAdapter.HolderConsulta, position: Int) {
        firestore = FirebaseFirestore.getInstance()

        with(holder) {
            consulta.text = consultaArrayList[position].consulta.toString()
        }
    }

    private fun getUserRol(position: Int) {
        var user = consultaArrayList[position].userID
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (consultaArrayList[position].userID == firebaseUser!!.uid) {
           Log.i("resp-type-0","here")
            MESSAGE_CONSULTA
        } else {
            Log.i("resp-type-1","here")
            MESSAGE_RESPUESTA
        }
    }
    override fun getItemCount(): Int {
        return consultaArrayList.size
    }

    inner class HolderConsulta(itemView: View) : RecyclerView.ViewHolder(itemView) {
       // var userc_consulta: TextView = itemView.find
        // ViewById(R.id.txt_consulta_user)
        //var name_consulta: TextView = itemView.findViewById(R.id.txt_consulta_name)

        var consulta: TextView = itemView.findViewById(R.id.txt_consulta_post)
        //var respuesta : TextView = itemView.findViewById(R.id.txt_consulta_respuesta_post)



    }


}