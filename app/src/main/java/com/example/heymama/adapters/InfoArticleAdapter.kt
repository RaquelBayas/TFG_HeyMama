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
import com.example.heymama.models.Article
import com.example.heymama.models.Post
import com.example.heymama.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InfoArticleAdapter(private val context: Context, private var articleArrayList: ArrayList<Article>, private val articleItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<InfoArticleAdapter.HolderArticle>() {

    private lateinit var dataBase: FirebaseDatabase

    fun filterList(list: ArrayList<Article>) {
        this.articleArrayList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArticle {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_info,parent,false)
        return HolderArticle(view)
    }

    override fun onBindViewHolder(holder: HolderArticle, position: Int) {
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")

        val tema_article_info: Article = articleArrayList[position] // get data at specific position
        holder.titulo_article.text = tema_article_info.title

        dataBase.reference.child("Usuarios").child(tema_article_info.professionalID.toString()).get().addOnSuccessListener {
            if(it.exists()) {
                var name = it.child("name").value.toString()
                var lastname = it.child("apellidos").value.toString()
                holder.autor_article.text = "$name $lastname"
            }
        }

        holder.titulo_article.setOnClickListener{
            articleItemListener.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return articleArrayList.size
    }

    inner class HolderArticle(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titulo_article: TextView = itemView.findViewById(R.id.titulo_article)
        var autor_article: TextView = itemView.findViewById(R.id.autor_article)
    }

}