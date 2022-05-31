package com.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.activities.CommentPostTLActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PostTimelineAdapter(private val context: Context, private val postsTimelineList: ArrayList<PostTimeline>, private val postTimelineListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<PostTimelineAdapter.Holder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var id_post: String
    private lateinit var id_user: String
    private lateinit var listener: ItemRecyclerViewListener

    /**
     * @param listener ItemRecyclerViewListener
     */
    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    /**
     * @param parent ViewGroup
     * @param viewType Int
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post,parent,false)
        return Holder(view,listener)
    }

    /**
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder:Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = firebaseStore.reference
        firestore = FirebaseFirestore.getInstance()

        val post_tl: PostTimeline = postsTimelineList[position]
        if(post_tl.userId!! == auth.uid){
            holder.btn_menu_post_tl.visibility = View.VISIBLE
            holder.btn_menu_post_tl.setOnClickListener {
                menuBtnPostTL(holder,post_tl)
            }
        }

        firestore.collection("Usuarios").document(post_tl.userId).addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            if(value!!.exists()) {
                val docs = value!!.data
                with(holder) {
                    name_post.text = docs!!["name"].toString()
                    image(holder.photo_post, post_tl.userId)
                    photo_post.setOnClickListener {
                        postTimelineListener.onItemClicked(position)
                    }
                    user_post.text = docs["username"].toString()
                    id_user = docs["id"].toString()
                    comment_post.text = post_tl.comment
                    commentCount_post.text = post_tl.commentCount.toString()
                    likeCount_post.text = post_tl.likeCount.toString()
                    id_post = post_tl.postId.toString()
                    val timestamp = post_tl.timestamp
                    val dateFormat = SimpleDateFormat("dd/MM/yy \n  HH:mm")
                    time_post.text = dateFormat.format(timestamp)
                    commentButton.setOnClickListener {
                        commentPostTL(name_post.text.toString(),
                            user_post.text.toString(),
                            comment_post.text.toString(),
                            postsTimelineList[Integer.parseInt(adapterPosition.toString())].postId.toString(),
                            postsTimelineList[Integer.parseInt(adapterPosition.toString())].userId.toString()
                        )
                    }
                }
            }
        }

        holder.likeButton.setOnClickListener {
           like(post_tl)
        }

        likeCounter(holder,position)
        commentsCounter(holder,position)
        changeButtonColors(holder, post_tl)
    }

    /**
     * Este método permite cargar la imagen del usuario
     * @param photoPost ImageView
     * @param userId String
     */
    private fun image(photoPost: ImageView, userId: String) {
        var photoRef = database.reference.child("Usuarios").child(userId).child("profilePhoto")
        photoRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists() && snapshot.value != ""){
                    storageReference = firebaseStore.getReference(snapshot.value.toString())
                    GlideApp.with(context)
                        .load(storageReference)
                        .error(R.drawable.wallpaper_profile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(photoPost)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    /**
     * Este método permite añadir un like en un post.
     * @param post_tl PostTimeline : Post
     */
    private fun like(post_tl: PostTimeline) {
        val likesReference =  firestore.collection("Likes").document(auth.uid.toString()).collection("Likes").document(post_tl.postId.toString())
        likesReference.get().addOnCompleteListener {
            if(!it.result.exists()) {
                val likesMap: HashMap<String, String> = HashMap()
                likesMap["postId"] = post_tl.postId.toString()
                likesMap["timestamp"] = Date().toString()
                likesReference.set(likesMap)
            } else {
                likesReference.delete()
            }
        }
        val postsLikedRef = firestore.collection("PostsLiked").document(post_tl.postId.toString()).collection("Users").document(auth.uid.toString())
        postsLikedRef.get().addOnCompleteListener {
            if(!it.result.exists()) {
                val usersLikes: HashMap<String,String> = HashMap()
                usersLikes["userId"] = auth.uid.toString()
                usersLikes["timestamp"] = Date().toString()
                postsLikedRef.set(usersLikes)
            } else {
                postsLikedRef.delete()
            }
        }

    }

    /**
     * Este método permite añadir un popupMenu a cada post de la timeline.
     * Cuenta con la opción 'eliminar' para borrar el post seleccionado.
     * @param holder Holder
     * @param post_tl PostTimeline
     */
    private fun menuBtnPostTL(holder: Holder,post_tl: PostTimeline) {
        val popupMenu = PopupMenu(context,holder.btn_menu_post_tl)
        popupMenu.menuInflater.inflate(R.menu.post_tl_menu,popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when(it.itemId) {
                R.id.eliminar_post_tl -> {
                    firestore.collection("Timeline").document(post_tl.postId.toString()).delete()
                    firestore.collection("Timeline").document(post_tl.postId.toString()).collection("Likes").get().addOnCompleteListener(object:OnCompleteListener<QuerySnapshot> {
                        override fun onComplete(p0: Task<QuerySnapshot>) {
                            for(doc in p0.result) {
                                firestore.collection("Timeline").document(post_tl.postId.toString()).collection("Likes").document(doc.id).delete()
                            }
                        }
                    })
                }
            }
            true
        })
    }

    /**
     * Este método cambia el color del botón "Like"
     * @param holder Holder
     * @param post_tl PostTimeline
     */
    private fun changeButtonColors(holder: Holder, post_tl: PostTimeline) {
        val timeline = firestore.collection("Timeline").orderBy("date", Query.Direction.ASCENDING)
        timeline.get().addOnCompleteListener {
            if(it.isSuccessful) {
                firestore.collection("Likes").document(auth.uid.toString()).collection("Likes").document(post_tl.postId.toString())
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        if (value!!.exists()) {
                            holder.likeButton.setButtonDrawable(R.drawable.ic_corazon_rojo)
                        } else {
                            holder.likeButton.setButtonDrawable(R.drawable.ic_corazon)
                        }
                    }
            }
        }
    }

    /**
     * Este método abre un nuevo activity dónde el usuario puede realizar un comentario dentro de un post.
     * @param name_post String
     * @param user_post String
     * @param comment_post String
     * @param id_post String
     * @param id_user String
     */
    private fun commentPostTL(name_post:String, user_post:String, comment_post:String, id_post:String, id_user:String) {
        val intent = Intent(context,CommentPostTLActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("name",name_post)
        intent.putExtra("user",user_post)
        intent.putExtra("comment",comment_post)
        intent.putExtra("idpost",id_post)
        intent.putExtra("iduser",id_user)
        this.context.startActivity(intent)
    }

    /**
     * Este método sirve para contar los comentarios recibidos en un post de la timeline.
     * @param holder PostTimelineAdapter
     * @param position Int
     */
    private fun commentsCounter(holder: PostTimelineAdapter.Holder, position: Int) {
        firestore.collection("Timeline").document(postsTimelineList[position].postId.toString())
            .collection("Replies").addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if(value!!.documents.isNotEmpty()) {
                    var count : String = value.documents.size.toString()
                    postsTimelineList[position].commentCount = Integer.parseInt(count)
                    firestore.collection("Timeline").document(postsTimelineList[position].postId.toString()).update("commentCount",Integer.parseInt(count))
                    holder.commentCount_post.text = count
                } else {
                    holder.commentCount_post.text = '0'.toString()
                }
            }
    }

    /**
     * Este método sirve para contar el número de likes de un post.
     * @param holder PostTimelineAdapter
     * @param position Int : Posición del post en el postsTimelineList
     */
    private fun likeCounter(holder:Holder, position:Int) {
        firestore.collection("PostsLiked").document(postsTimelineList[position].postId.toString()).collection("Users").addSnapshotListener { value, error ->
            if(error != null) { return@addSnapshotListener }
            if(value!!.documents.isNotEmpty()){
                val count : String = value.documents.size.toString()
                holder.likeCount_post.text = count
            } else {
                holder.likeCount_post.text = '0'.toString()
            }
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "postsTimelineList"
     */
    override fun getItemCount(): Int {
        return postsTimelineList.size
    }

    /**
     * ViewHolder
     */
    class Holder(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView){
        var user_post: TextView = itemView.findViewById(R.id.txt_tweet_user)
        var name_post: TextView = itemView.findViewById(R.id.txt_tweet_name)
        var photo_post: ImageView = itemView.findViewById(R.id.tweet_picture)
        var time_post: TextView = itemView.findViewById(R.id.txt_tweet_hora)
        var comment_post: TextView = itemView.findViewById(R.id.txt_tweet)
        var commentButton: ImageButton = itemView.findViewById(R.id.imageButtonComment)
        var commentCount_post: TextView = itemView.findViewById(R.id.txt_tweet_commentCount)
        var likeCount_post: TextView = itemView.findViewById(R.id.txt_tweet_LikeCount)
        var likeButton: ToggleButton = itemView.findViewById(R.id.imageButtonLike)
        var btn_menu_post_tl : Button = itemView.findViewById(R.id.btn_menu_post_tl)

        init {
            itemView.setOnClickListener {
                Log.i("PostTimelineAdapter",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }
}
