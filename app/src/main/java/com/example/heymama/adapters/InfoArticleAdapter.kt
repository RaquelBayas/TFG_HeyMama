package com.example.heymama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemForoListener
import com.example.heymama.models.Article
import com.example.heymama.models.Post

class InfoArticleAdapter(private val context: Context, private val articleArrayList: ArrayList<Article>, private val articleItemListener: ItemForoListener
) : RecyclerView.Adapter<InfoArticleAdapter.HolderArticle>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArticle {
        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_info,parent,false)
        return HolderArticle(view)
    }

    override fun onBindViewHolder(holder: HolderArticle, position: Int) {
        val tema_article_info: Article = articleArrayList[position] // get data at specific position
        holder.titulo_article.setText(tema_article_info.title)
        holder.titulo_article.setOnClickListener{
            articleItemListener.onItemForoClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return articleArrayList.size
    }

    inner class HolderArticle(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titulo_article: TextView = itemView.findViewById(R.id.titulo_article)
    }

}