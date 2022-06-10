package app.example.heymama.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import app.example.heymama.GlideApp
import app.example.heymama.R
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.PostTimeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CommentsPostTLAdapter(private val context: Context, private val idpost_origin: String, private val iduser_origin: String, private val commentsPostsList: ArrayList<PostTimeline>, private val commentsPostsListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<CommentsPostTLAdapter.Holder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var idUser: String

    private lateinit var onClickListener: ItemRecyclerViewListener

    fun ItemRecyclerViewListener() {
        onClickListener = commentsPostsListener
    }

    /**
     * @param parent ViewGroup
     * @param viewType Int
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsPostTLAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post_comment, parent, false)
        return Holder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentsPostTLAdapter.Holder, position: Int) {

        auth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        storageReference = firebaseStorage.reference
        firestore = FirebaseFirestore.getInstance()

        val post_tl: PostTimeline = commentsPostsList[position]
        val refPhoto = firebaseStorage.getReference("Usuarios/" + post_tl.userId + "/images/perfil")

        with(holder) {
            firestore.collection("Usuarios").document(post_tl.userId!!)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (value!!.exists()) {
                        val docs = value!!.data
                        name_post.text = docs!!["name"].toString()
                        user_post.text = docs["username"].toString()
                        idUser = docs["id"].toString()
                        comment_post.text = post_tl.comment
                        GlideApp.with(context.applicationContext)
                            .load(refPhoto)
                            .error(R.drawable.wallpaper_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(holder.photo_post)
                        val timestamp = post_tl.timestamp
                        val dateFormat = SimpleDateFormat("dd/MM/yy \n  HH:mm")
                        time_post.text = dateFormat.format(timestamp)
                    }
                }
        }
        database.reference.child("Usuarios").child(auth.uid.toString()).child("rol").get()
            .addOnSuccessListener {
                if (it.value == "Admin") {
                    menuComment(holder, post_tl)
                }
            }

        if (post_tl.userId!! == auth.uid) {
            menuComment(holder, post_tl)
        } else {
            menuReportPostTL(holder,post_tl)
        }
    }

    /**
     *
     * @param holder CommentsPostTLAdapter
     * @param post_tl PostTimeline
     */
    private fun menuReportPostTL(holder: CommentsPostTLAdapter.Holder, post_tl: PostTimeline) {
        holder.btn_comment_menu_post_tl.visibility = View.VISIBLE
        holder.btn_comment_menu_post_tl.setOnClickListener {
            val popupMenu = PopupMenu(context,holder.btn_comment_menu_post_tl)
            popupMenu.menuInflater.inflate(R.menu.post_tl_report_menu,popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when(it.itemId) {
                    R.id.reportar_post_tl -> {
                        val id = hashMapOf("postId" to post_tl.postId.toString())
                        val data = hashMapOf("postId" to post_tl.postId.toString(),
                            "userId" to auth.uid.toString(), "timestamp" to Date())
                        val ref = firestore.collection("PostsReported").document(post_tl.postId.toString())
                        ref.set(id)
                        ref.collection("ReportedBy").document(auth.uid.toString()).set(data)
                    }
                }
                true
            })
        }
    }

    /**
     * @param holder Holder
     * @param post_tl PostTimeline
     */
    private fun menuComment(holder: Holder, post_tl: PostTimeline) {
        holder.btn_comment_menu_post_tl.visibility = View.VISIBLE
        holder.btn_comment_menu_post_tl.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(context, holder.btn_comment_menu_post_tl)
            popupMenu.menuInflater.inflate(R.menu.post_tl_menu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when(it.itemId) {
                    R.id.eliminar_post_tl -> {
                        firestore.collection("Timeline").document(idpost_origin).collection("Replies").document(post_tl.postId.toString()).delete()
                        database.reference.child("NotificationsTL").child(iduser_origin).orderByChild("textpost").addValueEventListener(object:ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()) {
                                    snapshot.children.iterator().forEach { datasnapshot ->
                                        if(datasnapshot.exists()) {
                                            datasnapshot.children.iterator().forEach {
                                                if(it.exists()) {
                                                    if (it.value.toString() == post_tl.comment.toString()) {
                                                        datasnapshot.ref.removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
                true
            })
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "commentsPostsList"
     */
    override fun getItemCount(): Int {
        return commentsPostsList.size
    }

    /**
     * ViewHolder
     * @param itemView View
     */
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var user_post: TextView = itemView.findViewById(R.id.txt_user_comment_posttl)
        var name_post: TextView = itemView.findViewById(R.id.txt_name_comment_posttl)
        var photo_post: ImageView = itemView.findViewById(R.id.img_comment_posttl)
        var time_post: TextView = itemView.findViewById(R.id.txt_tweet_hora)
        var comment_post: TextView = itemView.findViewById(R.id.txt_comment_posttl)
        var btn_comment_menu_post_tl : Button = itemView.findViewById(R.id.btn_comment_menu_post_tl)
    }
}