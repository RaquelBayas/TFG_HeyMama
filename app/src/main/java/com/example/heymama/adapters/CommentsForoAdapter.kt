package com.example.heymama.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Comment
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.sql.Timestamp
import java.text.SimpleDateFormat

class CommentsForoAdapter(private val commentsForoArrayList: ArrayList<Comment>, private val foroItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<CommentsForoAdapter.HolderForo>() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var listener: ItemRecyclerViewListener

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsForoAdapter.HolderForo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_foro,parent,false)
        return HolderForo(view)
    }

    override fun onBindViewHolder(holder: CommentsForoAdapter.HolderForo, position: Int) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val tema_post: Comment = commentsForoArrayList[position]
        holder.comment_foro.text = tema_post.post

        if(tema_post.protected == "Público") {
            getDataUser(tema_post.userID,holder)
        }
        holder.comment_foro.setOnClickListener{
            foroItemListener.onItemClicked(position)
        }

        val timestamp = commentsForoArrayList[position].timestamp.toString()
        val timestamp1 = Timestamp.parse(timestamp)
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")

        holder.time.text = dateFormat.format(timestamp1)
    }

    /**
     * Este método permite obtener los datos del usuario
     * @param userID String
     * @param holder CommentsForoAdapter.HolderForo
     */
    private fun getDataUser(userID: String, holder: CommentsForoAdapter.HolderForo) {
        database.reference.child("Usuarios").child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user: User? = snapshot.getValue(User::class.java)
                    holder.user.visibility = View.VISIBLE
                    holder.user.text = user!!.username
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Devuelve la cantidad de elementos del arraylist.
     */
    override fun getItemCount(): Int {
        return commentsForoArrayList.size
    }

    /**
     * ViewHolder
     */
    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        val comment_foro: TextView = itemView.findViewById(R.id.textView8)
        val user: TextView = itemView.findViewById(R.id.txt_foro_name)
        val time: TextView = itemView.findViewById(R.id.txt_foro_time)
        init {
            itemView.setOnClickListener {
                Log.i("CommentsForoAdapter", listener.onItemClicked(adapterPosition).toString())
            }
            itemView.setOnLongClickListener {
                Log.i("CommentsForoAdapter", listener.onItemLongClicked(adapterPosition).toString())
            return@setOnLongClickListener true}
        }
    }

}