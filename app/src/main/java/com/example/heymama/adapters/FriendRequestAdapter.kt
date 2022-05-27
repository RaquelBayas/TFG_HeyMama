package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendRequestAdapter(private val context: Context, private val friendRequestList: ArrayList<FriendRequest>
) : RecyclerView.Adapter<FriendRequestAdapter.Holder>() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    /**
     *
     * @param parent ViewGroup
     * @param viewType Int
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendRequestAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friendrequests, parent, false)
        return Holder(view)
    }

    /**
     *
     * @param holder FriendRequestAdapter.HolderForo
     * @param position Int
     */
    override fun onBindViewHolder(holder: FriendRequestAdapter.Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = firebaseStore.reference
        dataBase = FirebaseDatabase.getInstance()
        dataBaseReference = dataBase.getReference("Usuarios")

        val friendRequest = friendRequestList[position].friend_send_uid //BUSCA EL USUARIO QUE ENVIÓ LA SOLICITUD

        dataBaseReference.child(friendRequest).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.txt_nombre_solicitud.text = snapshot.child("name").value.toString()
                holder.txt_user_solicitud.text = snapshot.child("username").value.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FriendRequestAdapter",error.toString())
            }
        })


        storageReference = firebaseStore.getReference("/Usuarios/"+friendRequest+"/images/perfil")
        GlideApp.with(context)
            .load(storageReference)
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(holder.img_solicitud)
        holder.img_solicitud.setOnClickListener {
            val intent = Intent(context,PerfilActivity::class.java)
            intent.putExtra("UserUID",friendRequest)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        holder.btn_aceptar_solicitud.setOnClickListener {
            acceptDenyFriendRequest(holder,"aceptar")
        }

        holder.btn_rechazar_solicitud.setOnClickListener {
            acceptDenyFriendRequest(holder,"rechazar")
        }
    }

    /**
     *
     * @param id String
     * @param request String
     *
     */
    private fun updateFriendRequest(id:String,request:String){
        var friendship_reference = firestore.collection("Friendship")

        // ACEPTAR -> ESTABLECE LA AMISTAD
        if(request.equals("aceptar")) {
            var friends = FriendRequest(auth.currentUser?.uid.toString(), id, "friends")
            friendship_reference.document(auth.currentUser?.uid.toString())
                .collection("Friends").document(id).set(friends)

            friendship_reference.document(id).collection("Friends")
                .document(auth.currentUser?.uid.toString()).set(friends)
        }
        // ELIMINA LA SOLICITUD DE AMISTAD
        deleteFriendRequest(friendship_reference,id)
    }

    /**
     *
     * @param reference CollectionReference
     * @param id String
     */
    private fun deleteFriendRequest(reference: CollectionReference, id: String) {
        reference.document(auth.currentUser?.uid.toString()).collection("FriendRequest")
            .document(id).delete()

        reference.document(id).collection("FriendRequest")
            .document(auth.currentUser?.uid.toString()).delete()
    }

    /**
     *
     * @param id String
     * @param request String
     */
    private fun searchFriendRequest(id: String, request: String) {
        firestore.collection("Friendship").document(auth.currentUser?.uid.toString()).collection("FriendRequest")
            .document(id).addSnapshotListener { value, error ->
                if(error != null) {
                    Log.e("FriendRequestAdapter",error.toString())
                    return@addSnapshotListener
                }
                if(value != null) {
                    val friendRequest = value.toObject(FriendRequest::class.java)
                    updateFriendRequest(id,request)

                    for(item in friendRequestList) {
                        val friend_receive = item.friend_receive_uid
                        val friend_send = item.friend_send_uid
                        val state = item.state
                        if (friendRequest != null) {
                            if ((friend_receive == friendRequest?.friend_receive_uid) && (friend_send == friendRequest.friend_send_uid)&& (state == friendRequest.state)) {
                                friendRequestList.remove(item)
                                notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
    }

    /**
     *
     * @param holder FriendRequestAdapter.HolderForo
     */
    private fun acceptDenyFriendRequest(holder: Holder, request: String) {
        val holder_username = holder.txt_user_solicitud.text.toString()
        //BUSCA EL ID DEL USERNAME CAPTURADO EN EL HOLDER
        dataBase.reference.child("Usuarios").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.iterator().forEach {
                    val user = it.getValue(User::class.java)
                    if(user!!.username == holder_username) {
                        searchFriendRequest(user.id.toString(),request)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "friendRequestList"
     */
    override fun getItemCount(): Int {
        return friendRequestList.size
    }

    /**
     *
     * @param itemView View
     */
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_nombre_solicitud: TextView = itemView.findViewById(R.id.txt_nombre_solicitud)
        var txt_user_solicitud: TextView = itemView.findViewById(R.id.txt_user_solicitud)
        var img_solicitud: ImageView = itemView.findViewById(R.id.img_solicitud)
        var btn_aceptar_solicitud: Button = itemView.findViewById(R.id.btn_aceptar_solicitud)
        var btn_rechazar_solicitud: Button = itemView.findViewById(R.id.btn_rechazar_solicitud)
    }
}

