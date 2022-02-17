package com.example.heymama.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Comment
import com.example.heymama.models.PostTimeline
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text

class PostTimelineAdapter(private val context: Context, private val postsTimelineList: ArrayList<PostTimeline>, private val postTimelineListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<PostTimelineAdapter.HolderForo>() {
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostTimelineAdapter.HolderForo {

        // inflate layout tema_foro.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_post,parent,false)
        return HolderForo(view)
    }

    override fun onBindViewHolder(holder: PostTimelineAdapter.HolderForo, position: Int) {

        val post_tl: PostTimeline = postsTimelineList[position] // get data at specific position
        holder.name_post.setText(post_tl.user?.name)
        //holder.img_tema_foro.setImageURI(tema_post)
        /*holder.user_post.setOnClickListener{
            postTimelineListener.onItemClicked(position)
        }*/
        holder.user_post.setText(post_tl.user?.username)
        holder.comment_post.setText(post_tl.comment)
        holder.commentCount_post.setText(post_tl.commentCount.toString())
        holder.likeCount_post.setText(post_tl.likeCount.toString())
    }

    override fun getItemCount(): Int {
        return postsTimelineList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){

        var user_post: TextView = itemView.findViewById(R.id.txt_tweet_user)
        var name_post: TextView = itemView.findViewById(R.id.txt_tweet_name)
        var comment_post: TextView = itemView.findViewById(R.id.txt_tweet)
        var commentCount_post: TextView = itemView.findViewById(R.id.txt_tweet_commentCount)
        var likeCount_post: TextView = itemView.findViewById(R.id.txt_tweet_LikeCount)

    }


}