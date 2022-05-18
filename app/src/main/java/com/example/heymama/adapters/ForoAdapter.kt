package com.example.heymama.adapters

import android.content.Context
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
import java.text.SimpleDateFormat

class ForoAdapter(private val context: Context, private var foroArrayList: ArrayList<Post>, private val foroItemListener: ItemRecyclerViewListener
    ) : RecyclerView.Adapter<ForoAdapter.HolderForo>() {

    private lateinit var database: FirebaseDatabase

    fun filterList(list: ArrayList<Post>) {
        this.foroArrayList = list
        notifyDataSetChanged()
    }

    /**
     *
     * @param parent ViewGroup
     * @param viewType Int
     *
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderForo {
        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_foro,parent,false)
        return HolderForo(view)
    }

    /**
     *
     * @param holder HolderForo
     * @param position Int
     */
    override fun onBindViewHolder(holder: HolderForo, position: Int) {
        database = FirebaseDatabase.getInstance()

        val tema_post: Post = foroArrayList[position] // get data at specific position
        if(tema_post.protected == "Público") {
            getDataUser(tema_post.userID, holder)
        }
        holder.titulo_foro.text = tema_post.title
        //holder.img_tema_foro.setImageURI(tema_post)
        holder.titulo_foro.setOnClickListener{
            foroItemListener.onItemClicked(position)
        }
        var timestamp = tema_post.timestamp
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")
        holder.time_foro.text = dateFormat.format(timestamp)
    }

    private fun getDataUser(userID: String, holder: HolderForo) {
        database.reference.child("Usuarios").child(userID).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                holder.user_foro.text = user!!.username
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
            }
        })
    }
    /**
     *
     * @param input
     *
     */
    override fun getItemCount(): Int {
        return foroArrayList.size
    }

    /**
     *
     * @param itemView View
     */
    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var titulo_foro: TextView = itemView.findViewById(R.id.textView8)
        var time_foro: TextView = itemView.findViewById(R.id.txt_foro_time)
        var user_foro: TextView = itemView.findViewById(R.id.txt_foro_name)
    }

}
