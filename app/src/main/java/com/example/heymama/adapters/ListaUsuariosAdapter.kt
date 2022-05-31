package com.example.heymama.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ListaUsuariosAdapter(private val context: Context, private var listaUsuarios: ArrayList<User>,private val usersListener: ItemRecyclerViewListener
) : RecyclerView.Adapter<ListaUsuariosAdapter.Holder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var rol: String
    private lateinit var listener: ItemRecyclerViewListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaUsuariosAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: ListaUsuariosAdapter.Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = firebaseStore.reference
        database = FirebaseDatabase.getInstance()
        dataBaseReference = database.getReference("Usuarios")

        getUsuarios(holder,position)
        getDataUser(holder,position)
    }

    /**
     * @param listener ItemRecyclerViewListener
     */
    fun setOnItemRecyclerViewListener(listener: ItemRecyclerViewListener) {
        this.listener = listener
    }

    private fun getDataUser(holder: Holder, position: Int) {
        database.reference.child("Usuarios").child(auth.uid.toString()).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                    if (rol == "Admin") {
                        holder.btn_menu_user.visibility = View.VISIBLE
                        holder.btn_menu_user.setOnClickListener {
                            menuUser(holder, listaUsuarios[position])
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun filterList(list: ArrayList<User>) {
        this.listaUsuarios = list
        notifyDataSetChanged()
    }

    private fun menuUser(holder: Holder, user: User,) {
        val popupMenu: PopupMenu = PopupMenu(context,holder.btn_menu_user)
        popupMenu.menuInflater.inflate(R.menu.post_tl_menu,popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            when(it.itemId) {
                R.id.eliminar_post_tl -> {
                    deleteUserAccount(user)
                }
            }
            true
        })
    }

    private fun deleteUserAccount(user: User) {
        var userId = user.id.toString()
        firestore.collection("Usuarios").document(userId).delete().addOnSuccessListener {
           database.getReference("Usuarios/$userId").removeValue()
        }
        database.getReference("Usernames/"+user.username).removeValue()
        database.getReference("Chats/$userId/Messages").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.iterator().forEach {
                    it.ref.removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        if(user.rol == "Profesional"){
            deleteUserArticulos(userId)
        }
        deleteUserConsultas(userId)
        deleteUserChats(userId)
        deleteUserLikes(userId)
        deleteChatList(userId)
        deleteUserFriends(userId)
        deleteUserFriendRequests(userId)
        deleteUserForos(userId)
        deleteUserMood(userId)
        deleteNotifications(userId)
        deleteUserPosts(userId)
        deleteUserPhotos(userId)
    }

    /**
     * Este método permite eliminar un chat iniciado con otro usuario
     * @param userId String: UID del usuario del chat
     */
    private fun deleteChatList(userId: String) {
        val ref = database.reference.child("ChatList")
        ref.child(userId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    it.ref.removeValue().addOnCompleteListener {
                        if(it.isSuccessful) {
                            Log.i("deleteChatlist","OK")
                        } else {
                            Log.i("deleteChatlist",it.toString())
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método elimina las solicitudes de amistad.
     * @param userId String : UID del usuario
     */
    private fun deleteUserFriendRequests(userId: String) {
        val reference = firestore.collection("Friendship")
        reference.document(userId).collection("FriendRequest").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                reference.document(it.id).collection("FriendRequest").document(userId).delete().addOnSuccessListener {
                    Log.i("deleteUserFriendRequest","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserFriendRequest",it.toString())
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserFriendRequest","OK-2")
                }.addOnFailureListener {
                    Log.i("deleteUserFriendRequest",it.toString())
                }
            }
        }
    }

    /**
     * Este método elimina los chats del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserChats(userId: String) {
        var chatsRef = database.reference.child("Chats")
        var chatListRef = database.reference.child("ChatList")
        chatListRef.child(userId).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.iterator().forEach {
                        chatListRef.child(it.key.toString()).child(userId).removeValue()
                    }
                    Log.i("deleteUserChatList", "Chats eliminados")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        var chats = chatsRef.child(userId).child("Messages")
        chats.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    snapshot.children.iterator().forEach {
                        chatsRef.child(it.key.toString()).child("Messages").child(userId).removeValue()
                        it.ref.removeValue().addOnSuccessListener {
                            Log.i("deleteUserChats","OK")
                        } }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método elimina los posts publicados por el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserPosts(userId: String) {
        var postsRef = firestore.collection("Timeline").whereEqualTo("userId",userId)
        postsRef.addSnapshotListener { value, error ->
            value?.documents?.iterator()?.forEach {
                it.reference.collection("Replies").addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach { it.reference.delete() }
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserPosts","OK")
                }.addOnFailureListener { Log.e("deleteUserPosts",it.toString()) }
            }
        }
        firestore.collection("Timeline").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.collection("Replies").whereEqualTo("userId",userId).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach { posts ->
                        posts.reference.delete().addOnSuccessListener {
                            Log.i("deleteUserPosts","OK-2")
                        }.addOnFailureListener { Log.e("deleteUserPosts",it.toString()) }
                    }
                }
            }
        }
    }

    /**
     * Este método elimina los registros de estado.
     * @param userId String : UID del usuario
     */
    private fun deleteUserMood(userId: String) {
        firestore.collection("Mood").document(userId).collection("Historial").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserMood","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserMood",it.toString())
                }
            }
        }
    }

    /**
     * Este método elimina los foros publicados y los comentarios.
     * @param userId String : UID del usuario
     */
    private fun deleteUserForos(userId: String){
        val temasForos = arrayListOf("Depresión","Embarazo","Posparto","Otros")
        firestore.collection("Foros").document("SubForos").addSnapshotListener { value, error ->
            temasForos.iterator().forEach {
                value!!.reference.collection(it).whereEqualTo("userID",userId).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach {
                        it.reference.collection("Comentarios").addSnapshotListener { value, error ->
                            value!!.documents.iterator().forEach { it.reference.delete() }
                            it.reference.delete().addOnSuccessListener {
                                Log.i("deleteForos","OK")
                            }.addOnFailureListener {
                                Log.i("deleteForos",it.toString())
                            }
                        }
                    }
                }
                value!!.reference.collection(it).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach{
                        it.reference.collection("Comentarios").whereEqualTo("userID",userId)
                            .addSnapshotListener { value, error ->
                                value!!.documents.iterator().forEach {
                                    it.reference.delete()
                                }
                            }
                    }
                }
            }
        }
    }

    /**
     * Este método elimina las notificaciones.
     * @param userId String : UID del usuario
     */
    private fun deleteNotifications(userId: String) {
        database.reference.child("NotificationsTL").child(userId).removeValue().addOnSuccessListener {
            Log.i("deleteNotifications","Notificaciones TL eliminadas")
        }
        database.reference.child("NotificationsConsultas").equalTo("uid",userId).ref.removeValue().addOnSuccessListener {
            Log.i("deleteNotifications","Notificaciones Consultas eliminadas")
        }
        var notiRef = database.reference.child("NotificationsTL")
        notiRef.get().addOnCompleteListener {
            it.result.children.iterator().forEach {
                it.children.iterator().forEach {
                    if(it.child("uid").value == userId) {
                        it.ref.removeValue()
                    }
                }
            }
        }
    }


    /**
     * Este método elimina las consultas del usuario
     * @param userId String: UID del usuario
     */
    private fun deleteUserConsultas(userId: String) {
        val arrayTemas = arrayListOf("Embarazo","Familia","Parto","Posparto","Otros")
        for(tema in arrayTemas) {
            firestore.collection("Consultas").document(tema).collection("Consultas").whereEqualTo("userID",userId).addSnapshotListener { value, error ->
                if(error != null) {
                    Log.e("SettingsActivity",error.toString())
                    return@addSnapshotListener
                }
                value!!.documents.iterator().forEach {
                    it.reference.collection("Respuestas").addSnapshotListener { value, error ->
                        if(error != null) {
                            Log.e("SettingsActivity",error.toString())
                        }
                        value!!.documents.iterator().forEach { it.reference.delete() }
                    }
                    it.reference.delete()
                    Log.i("deleteUserConsultas","OK")
                }
            }
        }
        arrayTemas.iterator().forEach {
            firestore.collection("Consultas").document(it).collection("Consultas").addSnapshotListener { value, error ->
                value!!.documents.iterator().forEach { it.reference.collection("Respuestas").whereEqualTo("userID",userId)
                    .addSnapshotListener { value, error ->  value!!.documents.iterator().forEach { it.reference.delete() } }}
            }
        }
    }

    /**
     * Este método, en el caso de los profesionales, elimina los artículos publicados.
     * @param userId String : UID del usuario
     */
    private fun deleteUserArticulos(userId: String) {
        firestore.collection("Artículos").whereEqualTo("professionalID",userId).addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach { it.reference.delete() }
        }
    }

    /**
     * Este método elimina las fotos del usuario (perfil,layout).
     * @param userId String : UID del usuario
     */
    private fun deleteUserPhotos(userId: String) {
        var perfilRef = firebaseStore.getReference("Usuarios/$userId/images/perfil")
        perfilRef.downloadUrl.addOnSuccessListener {
            perfilRef.delete()
            Log.i("deleteUserPhotos","OK")
        }.addOnFailureListener {
            Log.e("deleteUserPhotos",it.toString())
        }
        var layoutRef = firebaseStore.getReference("Usuarios/$userId/images/layout")
        layoutRef.downloadUrl.addOnSuccessListener {Log.i("deleteUserPhotoLayou","Ok")
        }.addOnFailureListener {
            Log.e("deleteUserPhotos",it.toString())
        }
    }

    /**
     * Este método elimina los posts a los que ha dado 'like' el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserLikes(userId: String) {
        firestore.collection("Likes").document(userId).collection("Likes").addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("deleteUserLikes",error.toString())
                return@addSnapshotListener
            }
            value!!.documents.iterator().forEach {
                firestore.collection("PostsLiked").document(it.id).collection("Users").document(userId).delete()
                it.reference.delete().addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.i("deleteUserLikes","OK")
                    } else {
                        Log.i("deleteUserLikes",it.toString())
                    }
                }
            }
        }

    }

    /**
     * Este método elimina los amigos del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserFriends(userId: String){
        val reference = firestore.collection("Friendship")
        reference.document(userId).collection("Friends").addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("deleteUserFriends-error",error.toString())
                return@addSnapshotListener
            }
            value!!.documents.iterator().forEach {
                reference.document(it.id).collection("Friends").document(userId).delete().addOnSuccessListener {
                    Log.i("deleteUserFriends","OK")
                }.addOnFailureListener {
                    Log.i("deleteUserFriends",it.toString())
                }
                it.reference.delete().addOnSuccessListener {
                    Log.i("deleteUserFriends","OK-2")
                }.addOnFailureListener {
                    Log.i("deleteUserFriends",it.toString())
                }
            }
        }
        reference.document(userId).delete()
    }

    private fun getUsuarios(holder: ListaUsuariosAdapter.Holder, position: Int) {
        var user = listaUsuarios[position]
        with(holder) {
            txt_nombre_user.text = user.name
            txt_username_user.text = user.username
            var uid = user.id
            storageReference = firebaseStore.getReference("/Usuarios/" + uid + "/images/perfil")
            GlideApp.with(context)
                .load(storageReference)
                .error(R.drawable.wallpaper_profile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(img_user)

            if(user.rol == "Profesional") {
                holder.verified.visibility = View.VISIBLE
            }
        }
    }
    /**
     * Devuelve la cantidad de elementos del arraylist "listaUsuarios"
     */
    override fun getItemCount(): Int {
        return listaUsuarios.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var txt_nombre_user: TextView = itemView.findViewById(R.id.txt_nombre_amigo)
        var txt_username_user: TextView = itemView.findViewById(R.id.txt_user_amigo)
        var img_user: ImageView = itemView.findViewById(R.id.img_amigos)
        var btn_menu_user: Button = itemView.findViewById(R.id.btn_menu_friends)
        var verified: ImageView = itemView.findViewById(R.id.verified)

        init {
            itemView.setOnClickListener {
                Log.i("ListaUsuariosAdapter",listener.onItemClicked(adapterPosition).toString())
            }
        }
    }

}