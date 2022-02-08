package com.example.heymama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.activities.TemaForoActivity
import com.example.heymama.interfaces.ItemForoListener
import com.example.heymama.models.Comment
import com.example.heymama.models.Post

class CommentsForoAdapter(private val context: Context, private val commentsForoArrayList: ArrayList<Comment>, private val foroItemListener: ItemForoListener
) : RecyclerView.Adapter<CommentsForoAdapter.HolderForo>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsForoAdapter.HolderForo {
        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_foro,parent,false)
        return HolderForo(view)
    }

    override fun onBindViewHolder(holder: CommentsForoAdapter.HolderForo, position: Int) {
        val tema_post: Comment = commentsForoArrayList[position] // get data at specific position
        holder.comment_foro.setText(tema_post.post)
        //holder.img_tema_foro.setImageURI(tema_post)
        holder.comment_foro.setOnClickListener{
            foroItemListener.onItemForoClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return commentsForoArrayList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var comment_foro: TextView = itemView.findViewById(R.id.textView8)
    }


}