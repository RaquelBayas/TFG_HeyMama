package com.example.heymama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Post

class ForoAdapter(private val context: Context, private val foroArrayList: ArrayList<Post>, private val foroItemListener: ItemRecyclerViewListener
    ) : RecyclerView.Adapter<ForoAdapter.HolderForo>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderForo {
        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_foro,parent,false)
        return HolderForo(view)
    }

    override fun onBindViewHolder(holder: HolderForo, position: Int) {
        val tema_post: Post = foroArrayList[position] // get data at specific position
        holder.titulo_foro.setText(tema_post.title)
        //holder.img_tema_foro.setImageURI(tema_post)
        holder.titulo_foro.setOnClickListener{
            foroItemListener.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return foroArrayList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titulo_foro: TextView = itemView.findViewById(R.id.textView8)
    }

}
