package app.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.R
import app.example.heymama.activities.CommentPostTLActivity
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.PostTimeline
import app.example.heymama.models.Report
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class ReportsAdapter(private val context: Context, private val reportsArrayList: ArrayList<Report>) : RecyclerView.Adapter<ReportsAdapter.Holder>() {

    private lateinit var listener: ItemRecyclerViewListener
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_report,parent,false)
        return Holder(view,listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val uid = reportsArrayList[position].userId
        database.reference.child("Usuarios").child(uid).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    holder.name.text = user!!.name
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        getComment(holder,reportsArrayList[position].postId)

    }

    private fun getComment(holder: Holder, idpost: String) {
        val ref = firestore.collection("Timeline")
        ref.document(idpost).addSnapshotListener { value, error ->
            if(value!!.exists()) {
                holder.comment.text = value!!["comment"].toString()
            } else {
                ref.addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        it.reference.collection("Replies").whereEqualTo("postId", idpost)
                            .addSnapshotListener { value, error ->
                                value!!.documents.iterator().forEach {
                                    val post = it.toObject(PostTimeline::class.java)
                                    holder.comment.text = post!!.comment.toString()
                                }
                            }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return reportsArrayList.size
    }

    class Holder(itemView: View,listener: ItemRecyclerViewListener): RecyclerView.ViewHolder(itemView){
        var name : TextView = itemView.findViewById(R.id.txt_report_user)
        var comment : TextView = itemView.findViewById(R.id.txt_report_post)
        var type: TextView = itemView.findViewById(R.id.txt_report_time)

        init {
            itemView.setOnClickListener {
                Log.i("PostTimelineAdapter",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }

}