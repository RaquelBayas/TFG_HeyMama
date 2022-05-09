package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Consulta
import com.google.firebase.firestore.FirebaseFirestore

class ConsultaAdapter(private val context: Context, private val consultasArrayList: ArrayList<Consulta>, private val consultaItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<ConsultaAdapter.HolderConsulta>() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var listener: ItemRecyclerViewListener

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderConsulta {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.consulta,parent,false)
        return HolderConsulta(view, listener)
    }

    override fun onBindViewHolder(holder: HolderConsulta, position: Int) {
        firestore = FirebaseFirestore.getInstance()

        getUserData(consultasArrayList[position].userID.toString(), holder)
        with(holder) {

            consulta.text = consultasArrayList[position].consulta.toString()

        }

    }

    /**
     * Este método permite obtener los datos del usuario a partir de su uid
     *
     * @param uid String
     * @param holder HolderConsulta
     *
     */
    private fun getUserData(uid: String, holder:HolderConsulta) {
        firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            val data = value!!.data
            holder.name_consulta.text = data!!["name"].toString()
            holder.userc_consulta.text = data!!["username"].toString()

        }
    }

    override fun getItemCount(): Int {
        return consultasArrayList.size
    }

    inner class HolderConsulta(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView){
        var userc_consulta: TextView = itemView.findViewById(R.id.txt_consulta_user)
        var name_consulta: TextView = itemView.findViewById(R.id.txt_consulta_name)
        var consulta: TextView = itemView.findViewById(R.id.txt_consulta_post)

        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
                true
            }
        }
    }
}