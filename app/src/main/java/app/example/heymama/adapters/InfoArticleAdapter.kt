package app.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.R
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.Article
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class InfoArticleAdapter(private val context: Context, private var articleArrayList: ArrayList<Article>, private val articleItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<InfoArticleAdapter.HolderArticle>() {

    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listener: ItemRecyclerViewListener

    /**
     * @param listener ItemRecyclerViewListener
     */
    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    fun filterList(list: ArrayList<Article>) {
        this.articleArrayList = list
        notifyDataSetChanged()
    }

    /**
     * @param parent ViewGroup
     * @param viewType Int
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderArticle {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_info,parent,false)
        return HolderArticle(view)
    }

    /**
     * @param holder HolderArticle
     * @param position Int
     */
    override fun onBindViewHolder(holder: HolderArticle, position: Int) {
        dataBase = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val tema_article_info: Article = articleArrayList[position]
        firestore.collection("Artículos").document(tema_article_info.idArticle.toString()).addSnapshotListener { value, error ->
            if(value!!.exists()) {
                holder.titulo_article.text = value["title"].toString()
            }
        }

        dataBase.reference.child("Usuarios").child(tema_article_info.professionalID.toString()).get().addOnSuccessListener {
            if(it.exists()) {
                holder.autor_article.text = it.child("name").value.toString()
            }
        }

        holder.titulo_article.setOnClickListener{
            articleItemListener.onItemClicked(position)
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "articleArraylist"
     */
    override fun getItemCount(): Int {
        return articleArrayList.size
    }

    inner class HolderArticle(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titulo_article: TextView = itemView.findViewById(R.id.titulo_article)
        var autor_article: TextView = itemView.findViewById(R.id.autor_article)
        init {
            itemView.setOnClickListener {
                Log.i("InfoArticleAdapter",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }
}