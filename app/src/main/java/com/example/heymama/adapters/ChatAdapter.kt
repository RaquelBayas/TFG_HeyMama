package com.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.activities.ViewFullImageActivity
import com.example.heymama.databinding.ItemChatLeftBinding
import com.example.heymama.databinding.ItemChatRightBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat

class ChatAdapter(private val context: Context, private val chatArrayList: ArrayList<Message>, private val chatListener: ItemRecyclerViewListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var firebaseUser: FirebaseUser ? = null
    private var storageReference: StorageReference ? = null
    private lateinit var listener: ItemRecyclerViewListener

    private val MESSAGE_LEFT_RECEIVER = 0
    private val MESSAGE_RIGHT_SENDER = 1

    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MESSAGE_RIGHT_SENDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right,parent,false)
            SendHolder(view,listener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left,parent,false)
            ReceiveHolder(view,listener)
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
                var timeMessage = getTime(message)
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
                var timeMessage = getTime(message)
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
                var timeMessage = getTime(message)
                viewHolder.binding_receive.itemChatTime.text = timeMessage
            }
        }
    }

    /**
     * Este método permite obtener y devolver la fecha del mensaje en el formato deseado.
     * @param message Message: Mensaje
     */
    private fun getTime(message: Message): String {
        var timestamp = message.timestamp
        val dateFormat = SimpleDateFormat("HH:mm")
        var time_message = dateFormat.format(timestamp)
        return time_message
    }

    /**
     *
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

    private fun deleteMsg() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.eliminar)
            .setMessage(R.string.alert_eliminar)
            .setNegativeButton("Cancelar") { view, _ ->
                Toast.makeText(context, "Cancel button pressed", Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setPositiveButton("Eliminar") { view, _ ->
                Toast.makeText(context,"Artículo eliminado", Toast.LENGTH_SHORT).show()
                view.dismiss()

            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    inner class SendHolder(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView) {
        var binding_send: ItemChatRightBinding = ItemChatRightBinding.bind(itemView)
        init {
            itemView.setOnClickListener {
                return@setOnClickListener
            }
            itemView.setOnLongClickListener {
                Log.i("ONCLICK: ",listener.onItemLongClicked(adapterPosition).toString())
                return@setOnLongClickListener true
            }
        }
    }
    inner class ReceiveHolder(itemView: View, listener:ItemRecyclerViewListener) : RecyclerView.ViewHolder(itemView){
        var binding_receive: ItemChatLeftBinding = ItemChatLeftBinding.bind(itemView)
        init {
            itemView.setOnClickListener { return@setOnClickListener }
            itemView.setOnLongClickListener {
                Log.i("ONCLICK: ",listener.onItemLongClicked(adapterPosition).toString())
                return@setOnLongClickListener true
            }
        }
    }


}