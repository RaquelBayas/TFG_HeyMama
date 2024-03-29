package app.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import app.example.heymama.GlideApp
import app.example.heymama.R
import app.example.heymama.activities.ViewFullImageActivity
import app.example.heymama.databinding.ItemChatLeftBinding
import app.example.heymama.databinding.ItemChatRightBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class ChatAdapter(private val context: Context, private val chatArrayList: ArrayList<Message>, private val chatListener: ItemRecyclerViewListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var firebaseUser: FirebaseUser ? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var storageReference: StorageReference ? = null
    private lateinit var listener: ItemRecyclerViewListener

    private val MESSAGE_LEFT_RECEIVER = 0
    private val MESSAGE_RIGHT_SENDER = 1

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    /**
     * Este método crea una vista diferente para cada mensaje del chat:
     * Si el usuario ha enviado un mensaje, este aparecerá a la derecha
     * Si el usuario ha recibido el mensaje, este aparecerá a la izquierda
     * @param parent ViewGroup
     * @param viewType Int
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MESSAGE_RIGHT_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right,parent,false)
            SendHolder(view,listener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left,parent,false)
            ReceiveHolder(view,listener)
        }
    }

    /**
     * @param holder RecyclerView.ViewHolder
     * @param position Int
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        val message = chatArrayList[position]

        if(holder.javaClass == SendHolder::class.java) {
            val viewHolder = holder as SendHolder
            if(message.imageUrl != "") {
                viewHolder.binding_send.txtMessageChat.visibility = View.GONE
                viewHolder.binding_send.imgChat.visibility = View.VISIBLE
                storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.imageUrl)
                GlideApp.with(context)
                    .load(storageReference)
                    .override(300)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.itemView.findViewById(R.id.img_chat))
                val timeMessage = getTime(message)
                viewHolder.binding_send.itemChatTime.text = timeMessage
                viewHolder.itemView.findViewById<ImageView>(R.id.img_chat).setOnClickListener {
                    val intent = Intent(context, ViewFullImageActivity::class.java)
                    intent.putExtra("url",message.imageUrl)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            } else {
                viewHolder.binding_send.txtMessageChat.visibility = View.VISIBLE
                viewHolder.binding_send.txtMessageChat.text = message.message
                viewHolder.binding_send.imgChat.visibility = View.GONE
                val timeMessage = getTime(message)
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
                    .override(300)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.itemView.findViewById(R.id.img_chat))
                val timeMessage = getTime(message)
                viewHolder.binding_receive.itemChatTime.text = timeMessage
                viewHolder.itemView.findViewById<ImageView>(R.id.img_chat).setOnClickListener {
                    val intent = Intent(context, ViewFullImageActivity::class.java)
                    intent.putExtra("url",message.imageUrl)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }
            } else {
                viewHolder.binding_receive.txtMessageChat.visibility = View.VISIBLE
                viewHolder.binding_receive.txtMessageChat.text = message.message
                viewHolder.binding_receive.imgChat.visibility = View.GONE
                val timeMessage = getTime(message)
                viewHolder.binding_receive.itemChatTime.text = timeMessage
            }
        }
    }

    /**
     * Este método permite obtener y devolver la fecha del mensaje en el formato deseado.
     * @param message Message: Mensaje
     */
    private fun getTime(message: Message): String {
        val timestamp = message.timestamp
        val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm")
        return dateFormat.format(timestamp)
    }

    /**
     * Este método carga una vista diferente dependiendo de si el mensaje lo ha enviado el usuario autentificado
     * o la persona con la que habla en el chat
     *@param position Int
     */
    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (chatArrayList[position].senderUID == firebaseUser!!.uid) {
            MESSAGE_RIGHT_SENDER
        } else {
            MESSAGE_LEFT_RECEIVER
        }
    }

    /**
     * Devuelve la cantidad de elementos del arraylist.
     */
    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    inner class SendHolder(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView) {
        var binding_send: ItemChatRightBinding = ItemChatRightBinding.bind(itemView)
        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
                return@setOnClickListener
            }
            itemView.setOnLongClickListener {
                Log.i("ONLONGCLICK: ",listener.onItemLongClicked(adapterPosition).toString())
                return@setOnLongClickListener true
            }
        }
    }
    inner class ReceiveHolder(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView){
        var binding_receive: ItemChatLeftBinding = ItemChatLeftBinding.bind(itemView)
        init {
            itemView.setOnClickListener {
                Log.i("ONCLICK: ",listener.onItemClicked(adapterPosition).toString())
                return@setOnClickListener }
            itemView.setOnLongClickListener {
                Log.i("ONLONGCLICK: ",listener.onItemLongClicked(adapterPosition).toString())
                return@setOnLongClickListener true
            }
        }
    }


}