package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ItemChatLeftBinding
import com.example.heymama.databinding.ItemChatRightBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Comment
import com.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class ChatAdapter(private val context: Context, private val chatArrayList: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var firebaseUser: FirebaseUser ? = null
    private var storageReference: StorageReference ? = null

    private val MESSAGE_LEFT_RECEIVER = 0
    private val MESSAGE_RIGHT_SENDER = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // inflate layout
        return if (viewType == MESSAGE_RIGHT_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right,parent,false)
            SendHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left,parent,false)
            ReceiveHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatArrayList[position]

        if(holder.javaClass == SendHolder::class.java) {
            val viewHolder = holder as SendHolder
            if(message.imageUrl != "") {
                viewHolder.binding_send.txtMessageChat.visibility = View.GONE
                viewHolder.binding_send.imgChat.visibility = View.VISIBLE
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.imageUrl)

                GlideApp.with(context)
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.itemView.findViewById(R.id.img_chat))
            } else {
                viewHolder.binding_send.txtMessageChat.text = message.message
                viewHolder.binding_send.imgChat.visibility = View.GONE
                var timeMessage = getTime(message)
                viewHolder.binding_send.itemChatTime.text = timeMessage

            }
        } else {
            val viewHolder = holder as ReceiveHolder
            if(message.imageUrl != "") {
                viewHolder.binding_receive.txtMessageChat.visibility = View.GONE
                viewHolder.binding_receive.imgChat.visibility = View.VISIBLE
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.imageUrl)

                GlideApp.with(context)
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.itemView.findViewById(R.id.img_chat))
            } else {
                viewHolder.binding_receive.txtMessageChat.text = message.message
                viewHolder.binding_receive.imgChat.visibility = View.GONE
            }
        }
    }

    fun getTime(message: Message): String {
        var timestamp = message.timestamp
        val dateFormat = SimpleDateFormat("HH:mm")
        var time_message = dateFormat.format(timestamp)
        return time_message
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (chatArrayList[position].senderUID == firebaseUser!!.uid) {
            MESSAGE_RIGHT_SENDER
        } else {
            MESSAGE_LEFT_RECEIVER
        }
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    inner class SendHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding_send: ItemChatRightBinding = ItemChatRightBinding.bind(itemView)
    }
    inner class ReceiveHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var binding_receive: ItemChatLeftBinding = ItemChatLeftBinding.bind(itemView)
    }


}