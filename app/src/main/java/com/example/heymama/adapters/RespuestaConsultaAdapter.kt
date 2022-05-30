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
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class RespuestaConsultaAdapter(private val context: Context, private val consultaArrayList: ArrayList<Consulta>) : RecyclerView.Adapter<RespuestaConsultaAdapter.HolderConsulta>() {
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase

    private val MESSAGE_CONSULTA = 0
    private val MESSAGE_RESPUESTA = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RespuestaConsultaAdapter.HolderConsulta {
        return if (viewType == MESSAGE_CONSULTA) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.consulta,parent,false)
            HolderConsulta(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.consulta_respuesta,parent,false)
            HolderConsulta(view)
        }
    }

    override fun onBindViewHolder(holder: RespuestaConsultaAdapter.HolderConsulta, position: Int) {
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()

        with(holder) {
            var userId = consultaArrayList[position].userID.toString()
            getDataUser(userId,holder,position)

            consulta.text = consultaArrayList[position].consulta.toString()
        }
    }

    private fun getDataUser(userId: String, holder: HolderConsulta, position: Int,) {
        database.reference.child("Usuarios").child(userId).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var user: User? = snapshot.getValue(User::class.java)
                    holder.username_consulta.text = user!!.username.toString()
                    holder.name_consulta.text = user!!.name.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (consultaArrayList[position].userID == firebaseUser!!.uid) {
            MESSAGE_CONSULTA
        } else {
            MESSAGE_RESPUESTA
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "consultaArrayList"
     */
    override fun getItemCount(): Int {
        return consultaArrayList.size
    }

    inner class HolderConsulta(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name_consulta: TextView = itemView.findViewById(R.id.txt_consulta_name)
        var username_consulta: TextView = itemView.findViewById(R.id.txt_consulta_user)
        var consulta: TextView = itemView.findViewById(R.id.txt_consulta_post)
    }

}