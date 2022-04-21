package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.ListChatItem
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text

class ListChatItemAdapter(private val context: Context, private val listChatItemsList: ArrayList<ListChatItem>, private val listChatItemsListener: ItemRecyclerViewListener
 ): RecyclerView.Adapter<ListChatItemAdapter.ChatItemForo>() {

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var idUser: String
    private val ONE_MEGABYTE: Long = 1024 * 1024
    private lateinit var listener: ItemRecyclerViewListener

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ListChatItemAdapter.ChatItemForo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_chats_item, parent, false)
        return ChatItemForo(view,listener)
    }

    override fun onBindViewHolder(holder: ListChatItemAdapter.ChatItemForo, position: Int) {
        firebaseStorage = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStorage.reference

        with(holder) {
            txt_name_chat_item.text = listChatItemsList[position].name
            txt_username_chat_item.text = listChatItemsList[position].username
            idUser = listChatItemsList[position].idUser

            storageReference = storageReference.child("Usuarios/"+idUser+"/images/perfil")

            storageReference
                .getBytes(8 * ONE_MEGABYTE).
                addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    img_chat_item.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                }

            txt_msg_chat_item.text = listChatItemsList[position].lastMessage
        }
    }

    override fun getItemCount(): Int {
        return listChatItemsList.size
    }



    inner class ChatItemForo(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView) {
        var txt_name_chat_item: TextView = itemView.findViewById(R.id.txt_name_chat_item)
        var txt_username_chat_item: TextView = itemView.findViewById(R.id.txt_username_chat_item)
        var img_chat_item: CircleImageView = itemView.findViewById(R.id.img_chat_item)
        var txt_msg_chat_item: TextView = itemView.findViewById(R.id.txt_msg_chat_item)

        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
            }
        }

    }

}