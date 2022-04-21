package com.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.models.ListChatItem
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class ListChatItemAdapter(private val context: Context, private val listChatItemsList: ArrayList<ListChatItem>
 ): RecyclerView.Adapter<ListChatItemAdapter.ChatItemForo>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ListChatItemAdapter.ChatItemForo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_chats_item, parent, false)
        return ChatItemForo(view)
    }

    override fun onBindViewHolder(holder: ListChatItemAdapter.ChatItemForo, position: Int) {
        with(holder) {
            txt_name_chat_item.text = listChatItemsList[position].name
            txt_username_chat_item.text = listChatItemsList[position].username
            img_chat_item.setImageResource(R.drawable.profile_picture)
            txt_msg_chat_item.text = listChatItemsList[position].lastMessage.toString()
            Log.i("CHAT-MSG",listChatItemsList[position].lastMessage)
        }

    }

    override fun getItemCount(): Int {
        return listChatItemsList.size
    }



    inner class ChatItemForo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_name_chat_item: TextView = itemView.findViewById(R.id.txt_name_chat_item)
        var txt_username_chat_item: TextView = itemView.findViewById(R.id.txt_username_chat_item)
        var img_chat_item: CircleImageView = itemView.findViewById(R.id.img_chat_item)
        var txt_msg_chat_item: TextView = itemView.findViewById(R.id.txt_msg_chat_item)

    }

}