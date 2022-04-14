package com.example.heymama.adapters

import android.content.ClipData
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.activities.CommentPostTLActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PostTimelineAdapter(private val context: Context, private val postsTimelineList: ArrayList<PostTimeline>, private val postTimelineListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<PostTimelineAdapter.Holder>() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var id_post: String
    private lateinit var id_user: String

    private lateinit var listener: ItemRecyclerViewListener

    enum class ViewHolderType {
        TYPE_TIMELINE, TYPE_PERFIL
    }

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder{
        /*return when(viewType) {
            ViewHolderType.TYPE_TIMELINE.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post,parent,false)
                return Holder(view,listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_perfil,parent,false)
                return Holder(view,listener)
            }

        }*/
        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post,parent,false)
        return Holder(view,listener)
    }

    /*override fun getItemViewType(position: Int): Int {
        return when(type) {
            "TIMELINE" -> ViewHolderType.TYPE_TIMELINE.ordinal
            "PERFIL" -> ViewHolderType.TYPE_PERFIL.ordinal
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostTimelineAdapter.Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference

        firestore = FirebaseFirestore.getInstance()

        //postsTimelineList.sortedDescending()
        var post_tl: PostTimeline = postsTimelineList[position] // get data at specific position
        //var refPhoto = post_tl.user?.profilePhoto
        var refPhoto = storageReference.child("Usuarios/"+post_tl.userId+"/images/perfil").toString()
        storageReference = storageReference.child("Usuarios/"+post_tl.userId+"/images/perfil")

        val ONE_MEGABYTE: Long = 1024 * 1024
        var uri: Uri = Uri.parse(refPhoto)

        Log.i("URI 0: ", "parse profile photo1: " + position);
        Log.i("URI 0: ", "parse profile photo2: " + uri);
        Log.i("URI 0: ", "parse profile photo3: " + storageReference);


        if(post_tl.userId!!.equals(auth.uid)){
            holder.btn_menu_post_tl.visibility = View.VISIBLE
            holder.btn_menu_post_tl.setOnClickListener {
                val popupMenu: PopupMenu = PopupMenu(context,holder.btn_menu_post_tl)
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
        }

        firestore.collection("Usuarios").document(post_tl.userId!!).addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            val docs = value!!.data
            with(holder) {
                name_post.text = docs!!["name"].toString()
                storageReference
                    .getBytes(8 * ONE_MEGABYTE).addOnSuccessListener { bytes ->
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        holder.photo_post.setImageBitmap(bmp)
                    }.addOnFailureListener {
                        Log.e(TAG, "Se produjo un error al descargar la imagen.", it)
                    }
                photo_post.setOnClickListener {
                    postTimelineListener.onItemClicked(position)
                }
                user_post.text = docs["username"].toString()
                id_user = docs["ID"].toString()
                comment_post.text = post_tl.comment
                commentCount_post.text = post_tl.commentCount.toString() //likeCounter(holder,position).toString()
                likeCount_post.text = post_tl.likeCount.toString()
                id_post = post_tl.postId.toString()
            }
        }
        /*with(holder) {
            name_post.setText(post_tl.user?.name)
            user_post.text = post_tl.user?.username
            id_user = post_tl.user?.id.toString()
            comment_post.text = post_tl.comment
            commentCount_post.text = post_tl.commentCount.toString() //likeCounter(holder,position).toString()
            likeCount_post.text = post_tl.likeCount.toString()
            id_post = post_tl.postId.toString()

            /*var dateFormat = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy")

            var date : LocalDate = LocalDate.parse(post_tl.date, DateTimeFormatter.ISO_DATE)
            var datepost = post_tl.date.toString()

            //Log.i("date",datepost)
            var dateFormat2 = SimpleDateFormat("dd/MM/yyyy")
            Log.i("date", dateFormat2.parse(datepost).toString())
            time_post.text = datepost//dateFormat2.format(post_tl.date).toString()
            //time_post.text = dateFormat2.format(date.time.toString()).toString()
*/
        }*/
        holder.likeButton.setOnClickListener { //object: View.OnClickListener {
            //override fun onClick(p0: View?) {
            firestore.collection("Timeline").addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                firestore.collection("Timeline").document(post_tl.postId.toString()).collection("Likes")
                    .document(auth.uid.toString()).get().addOnCompleteListener {
                        if (!it.result.exists()) {
                            val likesMap: HashMap<String, String> = HashMap()
                            likesMap?.put("timestap", Date().toString())
                            firestore.collection("Timeline").document(post_tl.postId.toString())
                                .collection("Likes").document(auth.uid.toString()).set(
                                    likesMap!!
                                )
                        } else {
                            firestore.collection("Timeline").document(post_tl.postId.toString()).collection("Likes")
                                .document(auth.uid.toString()).delete()
                        }
                    }
            }
        }
        //CONTADOR DE LIKES
        likeCounter(holder,position)
        //CONTADOR DE COMENTARIOS
        commentsCounter(holder,position)

        // CAMBIAR COLOR DE LOS BOTONES
        val timeline = firestore.collection("Timeline").orderBy("date", Query.Direction.ASCENDING)
        timeline.get().addOnCompleteListener {
            if(it.isSuccessful) {
                firestore.collection("Timeline")
                    .document(post_tl.postId.toString() )
                    .collection("Likes").document(auth.uid.toString())
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
        with(holder) {
            commentButton.setOnClickListener {
                commentPostTL(name_post.text.toString(),user_post.text.toString(),comment_post.text.toString(), postsTimelineList[Integer.parseInt(adapterPosition.toString())].postId.toString(), id_user
                )
            }
        }

    }



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

    private fun commentsCounter(holder: PostTimelineAdapter.Holder, position: Int) {
        firestore.collection("Timeline").document(postsTimelineList[position].postId.toString())
            .collection("Replies").addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if(value!!.documents.isNotEmpty()) {
                    var count : String = value.documents!!.size.toString()
                    holder.commentCount_post.text = count
                } else {
                    holder.commentCount_post.text = '0'.toString()
                }
            }
    }
    private fun likeCounter(holder:PostTimelineAdapter.Holder, position:Int) {
        firestore.collection("Timeline").document(postsTimelineList[position].postId.toString())
            .collection("Likes").addSnapshotListener { value, error ->
                if(error != null) {
                    return@addSnapshotListener
                }
                if(value!!.documents.isNotEmpty()) {
                    var count : String = value.documents!!.size.toString()
                    holder.likeCount_post.text = count
                } else {
                    holder.likeCount_post.text = '0'.toString()
                }
            }
    }

    override fun getItemCount(): Int {
        return postsTimelineList.size
    }

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
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }



}
