package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.ListChatItem
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ListChatItemAdapter(private val context: Context, private val listChatItemsList: ArrayList<ListChatItem>, private val listChatItemsListener: ItemRecyclerViewListener
 ): RecyclerView.Adapter<ListChatItemAdapter.ChatItemForo>() {

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore

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
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
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
            var status = listChatItemsList[position].status
            if(status == "online"){
                img_chat_status.setImageResource(R.drawable.ic_online)
            } else {
                img_chat_status.setImageResource(R.drawable.ic_offline)
            }
        }

        menuItem(holder)
    }

    private fun menuItem(holder: ChatItemForo) {
        holder.btn_menu_list_chats_item.visibility = View.VISIBLE
        holder.btn_menu_list_chats_item.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(context,holder.btn_menu_list_chats_item)
            popupMenu.menuInflater.inflate(R.menu.post_tl_menu,popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                when(it.itemId) {
                    R.id.eliminar_post_tl -> {
                        deleteChat(idUser)
                    }
                }
                true
            })
        }
    }


    private fun deleteChat(idUser: String) {

        dataBase.reference.child("Chats").child(auth.uid.toString()).child("Messages").child(idUser).removeValue().addOnSuccessListener {
            Toast.makeText(context,"Chat eliminado",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Utils.showError(context,"Se ha producido un error.")
        }

    }

    override fun getItemCount(): Int {
        return listChatItemsList.size
    }



    inner class ChatItemForo(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView) {
        var txt_name_chat_item: TextView = itemView.findViewById(R.id.txt_name_chat_item)
        var txt_username_chat_item: TextView = itemView.findViewById(R.id.txt_username_chat_item)
        var img_chat_item: CircleImageView = itemView.findViewById(R.id.img_chat_item)
        var img_chat_status: ImageView = itemView.findViewById(R.id.img_chat_status)
        var txt_msg_chat_item: TextView = itemView.findViewById(R.id.txt_msg_chat_item)
        var btn_menu_list_chats_item: Button = itemView.findViewById(R.id.btn_menu_list_chats_item)

        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
            }
        }

    }

}