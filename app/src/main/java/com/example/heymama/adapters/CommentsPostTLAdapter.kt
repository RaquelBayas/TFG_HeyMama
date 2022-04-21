package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentsPostTLAdapter(private val context: Context, private val idpost_origin: String, private val commentsPostsList: ArrayList<PostTimeline>, private val commentsPostsListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<CommentsPostTLAdapter.HolderForo>() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var id_post: String
    private lateinit var id_user: String

    private lateinit var onClickListener: ItemRecyclerViewListener

    fun ItemRecyclerViewListener() {
        onClickListener = commentsPostsListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsPostTLAdapter.HolderForo {

        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post_comment,parent,false)
        return HolderForo(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentsPostTLAdapter.HolderForo, position: Int) {
        var idpost_origin = idpost_origin

        auth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference

        firestore = FirebaseFirestore.getInstance()

        val post_tl: PostTimeline = commentsPostsList[position] // get data at specific position
        val refPhoto = storageReference.child("Usuarios/"+post_tl.userId+"/images/perfil").toString()//post_tl.user?.profilePhoto
        //Log.i("URI: ", "parse profile photo1: " + post_tl.user.toString());
        storageReference = storageReference.child("Usuarios/"+post_tl.userId+"/images/perfil")

        val ONE_MEGABYTE: Long = 1024 * 1024
        var uri : Uri = Uri.parse(refPhoto)


        Log.i("URI: ", "parse profile photo2: " + uri);
        Log.i("URI: ", "parse profile photo3: " + storageReference);

        with(holder) {
            firestore.collection("Usuarios").document(post_tl.userId!!).addSnapshotListener { value, error ->
                if(error != null) {
                    return@addSnapshotListener
                }
                val docs = value!!.data
                name_post.text = docs!!["name"].toString()
                user_post.text = docs["username"].toString()
                id_user = docs["id"].toString()
                comment_post.text = post_tl.comment
                storageReference
                    .getBytes(8 * ONE_MEGABYTE).
                    addOnSuccessListener { bytes ->
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        holder.photo_post.setImageBitmap(bmp)
                    }.addOnFailureListener {
                        Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                    }
            }
        }

        if(post_tl.userId!!.equals(auth.uid)) {
            holder.btn_comment_menu_post_tl.visibility = View.VISIBLE
            holder.btn_comment_menu_post_tl.setOnClickListener {
                val popupMenu: PopupMenu = PopupMenu(context, holder.btn_comment_menu_post_tl)
                popupMenu.menuInflater.inflate(R.menu.post_tl_menu, popupMenu.menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.eliminar_post_tl -> {
                            /*firestore.collection("Timeline").document(idpost_origin).collection("Replies").get().addOnCompleteListener(object:
                                OnCompleteListener<QuerySnapshot> {
                                override fun onComplete(p0: Task<QuerySnapshot>) {
                                    for(doc in p0.result) {
                                        firestore.collection("Timeline").document(idpost_origin).collection("Replies").document(doc.id).delete()
                                    }
                                }
                            })*/
                            firestore.collection("Timeline").document(idpost_origin).collection("Replies").document(post_tl.postId.toString()).delete()
                        }
                    }
                    true
                })
            }
        }
        //DESCOMENTAR
       /* with(holder) {
            name_post.setText(post_tl.user?.name)
            storageReference
                .getBytes(8 * ONE_MEGABYTE).
                addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.photo_post.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                }
            photo_post.setOnClickListener {
                commentsPostsListener.onItemClicked(position)
            }
            user_post.text = post_tl.user?.username
            id_user = post_tl.user?.id.toString()
            comment_post.text = post_tl.comment
            id_post = post_tl.postId.toString()


        }*/

    }


    override fun getItemCount(): Int {
        return commentsPostsList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var user_post: TextView = itemView.findViewById(R.id.txt_user_comment_posttl)
        var name_post: TextView = itemView.findViewById(R.id.txt_name_comment_posttl)
        var photo_post: ImageView = itemView.findViewById(R.id.img_comment_posttl)
        //var time_post: TextView = itemView.findViewById(R.id.txt_tweet_hora)
        var comment_post: TextView = itemView.findViewById(R.id.txt_comment_posttl)
        var btn_comment_menu_post_tl : Button = itemView.findViewById(R.id.btn_comment_menu_post_tl)
    }



}