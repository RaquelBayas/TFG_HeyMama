package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Comment
import com.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatAdapter(private val context: Context, private val chatArrayList: ArrayList<Message>, private val chatItemListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<ChatAdapter.HolderForo>() {

    private var firebaseUser: FirebaseUser ? = null

    private val MESSAGE_LEFT_RECEIVER = 0
    private val MESSAGE_RIGHT_SENDER = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatAdapter.HolderForo {
        // inflate layout
        return if (viewType == MESSAGE_RIGHT_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right,parent,false)
            HolderForo(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left,parent,false)
            HolderForo(view)
        }
    }

    override fun onBindViewHolder(holder: ChatAdapter.HolderForo, position: Int) {
        val message = chatArrayList[position]
        holder.msg_chat.text = message.message

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

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView){
        var msg_chat: TextView = itemView.findViewById(R.id.txt_message_chat)
    }


}