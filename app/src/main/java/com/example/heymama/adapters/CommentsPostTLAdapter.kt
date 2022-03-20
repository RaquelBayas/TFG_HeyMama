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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentsPostTLAdapter(private val context: Context, private val commentsPostsList: ArrayList<PostTimeline>, private val commentsPostsListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<CommentsPostTLAdapter.HolderForo>() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

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
        auth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference

        firestore = FirebaseFirestore.getInstance()

        val post_tl: PostTimeline = commentsPostsList[position] // get data at specific position
        val refPhoto = post_tl.user?.profilePhoto
        Log.i("URI: ", "parse profile photo1: " + post_tl.user.toString());
        storageReference = storageReference.child(refPhoto.toString())

        val ONE_MEGABYTE: Long = 1024 * 1024
        var uri : Uri = Uri.parse(refPhoto)


        Log.i("URI: ", "parse profile photo2: " + uri);
        Log.i("URI: ", "parse profile photo3: " + storageReference);

        with(holder) {
            user_post.text = post_tl.user?.username
            name_post.text = post_tl.user?.name
            id_user = post_tl.user?.id.toString()
            comment_post.text = post_tl.comment
        }

        with(holder) {
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


        }

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

    }



}