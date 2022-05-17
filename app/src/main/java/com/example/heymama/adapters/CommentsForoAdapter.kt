package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.activities.TemaForoActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Comment
import com.example.heymama.models.Post
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentsForoAdapter(private val context: Context, private val commentsForoArrayList: ArrayList<Comment>, private val foroItemListener: ItemRecyclerViewListener
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

        val tema_post: Comment = commentsForoArrayList[position] // get data at specific position
        holder.comment_foro.text = tema_post.post
        //holder.img_tema_foro.setImageURI(tema_post)
        if(tema_post.protected == "Público") {
            getDataUser(tema_post.userID,holder)
        }
        holder.comment_foro.setOnClickListener{
            foroItemListener.onItemClicked(position)
        }
    }

    private fun getDataUser(userID: String, holder: CommentsForoAdapter.HolderForo) {
        database.reference.child("Usuarios").child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                holder.user.text = user!!.username
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
    }

    override fun getItemCount(): Int {
        return commentsForoArrayList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var comment_foro: TextView = itemView.findViewById(R.id.textView8)
        var user: TextView = itemView.findViewById(R.id.txt_foro_name)
        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }

}