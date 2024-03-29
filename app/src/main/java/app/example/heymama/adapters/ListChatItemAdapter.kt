package app.example.heymama.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import app.example.heymama.GlideApp
import app.example.heymama.R
import app.example.heymama.Utils
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.ListChat
import app.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ListChatItemAdapter(private val context: Context, private val listChatItemsList: ArrayList<ListChat>
 ): RecyclerView.Adapter<ListChatItemAdapter.ChatItemForo>() {

    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listener: ItemRecyclerViewListener

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    /**
     * @param parent ViewGroup
     * @param viewType Int
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): ListChatItemAdapter.ChatItemForo {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_chats_item, parent, false)
        return ChatItemForo(view,listener)
    }

    /**
     * @param holder ListChatItemAdapter.ChatItemForo
     * @param position Int
     */
    override fun onBindViewHolder(holder: ListChatItemAdapter.ChatItemForo, position: Int) {
        firebaseStorage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storageReference = firebaseStorage.reference
        var idUser = listChatItemsList[position].id.toString()
        with(holder){

            database.reference.child("Usuarios").child(idUser).addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        var user = snapshot.getValue(User::class.java)
                        txt_name_chat_item.text = user!!.name
                        txt_username_chat_item.text = user.username
                        storageReference =
                            firebaseStorage.getReference("Usuarios/" + idUser + "/images/perfil")
                        GlideApp.with(context)
                            .load(storageReference)
                            .error(R.drawable.wallpaper_profile)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(img_chat_item)
                        var status = user.status
                        if (status == "online") {
                            img_chat_status.setImageResource(R.drawable.ic_online)
                        } else {
                            img_chat_status.setImageResource(R.drawable.ic_offline)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        menuItem(holder,idUser)
    }

    /**
     * Este método permite mostrar el menú para eliminar un chat.
     * @param holder ChatItemForo
     * @param idUser String
     */
    private fun menuItem(holder: ChatItemForo, idUser: String) {
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

    /**
     * Este método permite eliminar un chat
     * @param idUser String : UID del usuario
     */
    private fun deleteChat(idUser: String) {
        var ref = database.reference.child("ChatList").child(auth.uid.toString()).child(idUser)
        if (!Utils.isNetworkAvailable(context)) {
            Utils.alertDialogInternet(context)
        } else if (Utils.isNetworkAvailable(context)) {
            ref.removeValue().addOnSuccessListener {
                database.reference.child("Chats").child(auth.uid.toString()).child("Messages")
                    .child(idUser)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                snapshot.children.iterator().forEach { it.ref.removeValue() }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                Utils.showToast(context, "Chat eliminado")

            }.addOnFailureListener {
                Utils.showToast(context, "Se ha producido un error.")
            }
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist.
     */
    override fun getItemCount(): Int {
        return listChatItemsList.size
    }

    /**
     * ViewHolder
     */
    inner class ChatItemForo(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView) {
        var txt_name_chat_item: TextView = itemView.findViewById(R.id.txt_name_chat_item)
        var txt_username_chat_item: TextView = itemView.findViewById(R.id.txt_username_chat_item)
        var img_chat_item: CircleImageView = itemView.findViewById(R.id.img_chat_item)
        var img_chat_status: ImageView = itemView.findViewById(R.id.img_chat_status)
        var btn_menu_list_chats_item: Button = itemView.findViewById(R.id.btn_menu_list_chats_item)

        init {
            itemView.setOnClickListener {
                Log.i("ListChatItemAdapter",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }

}