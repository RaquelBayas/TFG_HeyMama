package com.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.activities.CommentPostTLActivity
import com.example.heymama.activities.RespuestaConsultaActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Consulta
import com.example.heymama.models.Notification
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class NotificationsAdapter(private val context: Context?, private val notificationsList: ArrayList<Notification>, private val notificationsListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<NotificationsAdapter.Holder>() {
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listener: ItemRecyclerViewListener

    /**
     *
     * @param listener ItemRecyclerViewListener
     *
     */
    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationsAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_notification,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: NotificationsAdapter.Holder, position: Int) {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        with(holder) {
            val uid = notificationsList[position].uid.toString()
            comment.text = notificationsList[position].textpost
            type.text = notificationsList[position].type
            database.reference.child("Usuarios").child(uid).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        name.text = user!!.name

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
            if(type.text == "ha comentado en tu post") {
                comment.setOnClickListener { comment(name.text.toString(),comment.text.toString(),notificationsList[position].idpost.toString(),auth.uid.toString()) }
            } else {
                comment.setOnClickListener {
                    consulta(notificationsList[position].idpost.toString())
                }
            }
        }
    }

    /**
     * Este método permite buscar la consulta en la base de datos a partir del id de la misma
     */
    private fun consulta(idconsulta: String) {
        firestore.collection("Consultas").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.collection("Consultas").whereEqualTo("id",idconsulta).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        val consulta = it.toObject(Consulta::class.java)
                        openConsulta(consulta!!)
                    }
                }
            }
        }
    }

    /**
     * Este método permite abrir la consulta seleccionada.
     * @param consulta Consulta
     */
    private fun openConsulta(consulta: Consulta) {
        val intent = Intent(context, RespuestaConsultaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("id_consulta",consulta.id)
        intent.putExtra("tema_consulta",consulta!!.tema)
        intent.putExtra("id_user_consulta",consulta.userID)
        intent.putExtra("consulta",consulta)
        this.context!!.startActivity(intent)
    }

    private fun comment(name: String,comment: String, idpost: String, iduser: String) {
        val intent = Intent(context, CommentPostTLActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("name",name)
        intent.putExtra("comment",comment)
        intent.putExtra("idpost",idpost)
        intent.putExtra("iduser",iduser)
        this.context!!.startActivity(intent)
    }

    override fun getItemCount(): Int {
       return notificationsList.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name : TextView = itemView.findViewById(R.id.txt_user_notification)
        var comment : TextView = itemView.findViewById(R.id.txt_post_notification)
        var type: TextView = itemView.findViewById(R.id.txt_type_notification)
    }

}