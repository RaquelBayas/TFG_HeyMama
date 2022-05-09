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
import com.google.firebase.auth.FirebaseAuth

class CommentsForoAdapter(private val context: Context, private val commentsForoArrayList: ArrayList<Comment>, private val foroItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<CommentsForoAdapter.HolderForo>() {

    private lateinit var auth : FirebaseAuth
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

        val tema_post: Comment = commentsForoArrayList[position] // get data at specific position
        holder.comment_foro.setText(tema_post.post)
        //holder.img_tema_foro.setImageURI(tema_post)
        holder.comment_foro.setOnClickListener{
            foroItemListener.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return commentsForoArrayList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var comment_foro: TextView = itemView.findViewById(R.id.textView8)

        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }

}