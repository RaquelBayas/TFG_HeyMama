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
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ListaUsuariosAdapter(private val context: Context, private var listaUsuarios: ArrayList<User>
) : RecyclerView.Adapter<ListaUsuariosAdapter.Holder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var rol: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaUsuariosAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend,parent,false)
        return ListaUsuariosAdapter.Holder(view)
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

    private fun getDataUser(holder: Holder, position: Int) {
        database.reference.child("Usuarios").child(auth.uid.toString()).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user : User? = snapshot.getValue(User::class.java)
                rol = user!!.rol.toString()
                if(rol == "Admin") {
                    holder.btn_menu_user.visibility = View.VISIBLE
                    holder.btn_menu_user.setOnClickListener {
                        menuUser(holder,listaUsuarios[position])
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //TO DO("Not yet implemented")
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
        database.getReference("Chats/"+userId+"/Messages").addValueEventListener(object: ValueEventListener{
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

    private fun deleteChatList(userId: String) {
        val ref = database.reference.child("ChatList")
        ref.child(userId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    Log.i("DELETE-CHATLIST",it.toString())
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
                reference.document(it.id).collection("FriendRequest").document(userId).delete()
            }
        }
    }

    /**
     * Este método elimina los chats del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserChats(userId: String) {
        database.reference.child("ChatList").child(userId).removeValue()
        database.reference.child("Chats").child(userId).child("Messages").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    snapshot.children.iterator().forEach { it.ref.removeValue() }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ListaUsuariosAdapter",error.toString())
            }
        })
    }

    /**
     * Este método elimina los posts publicados por el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserPosts(userId: String) {
        firestore.collection("Timeline").whereEqualTo("userId",userId).addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.delete()
            }
        }
        firestore.collection("Timeline").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.collection("Replies").whereEqualTo("userId",userId).addSnapshotListener { value, error ->
                    value!!.documents.iterator().forEach { posts ->
                        posts.reference.delete()
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
                it.reference.delete()
            }
        }
    }

    /**
     * Este método elimina los foros publicados y los comentarios.
     * @param userId String : UID del usuario
     */
    private fun deleteUserForos(userId: String){
        var temasForos = arrayListOf("Depresión","Embarazo","Posparto","Otros")
        for(temaForo in temasForos) {
            firestore.collection("Foros").document("SubForos").collection(temaForo).whereEqualTo("userID",userId)
                .addSnapshotListener { value, error ->
                    if(error != null) {
                        Log.e("SettingsActivity",error.toString())
                        return@addSnapshotListener
                    }
                    for(doc in value!!.documents) {
                        doc.reference.delete().addOnSuccessListener {
                            doc.reference.collection("Comentarios").addSnapshotListener { value, error ->
                                if(error != null) {
                                    Log.e("SettingsActivity",error.toString())
                                }
                                for (doc in value!!.documents) {
                                    doc.reference.delete()
                                }
                            }
                        }.addOnFailureListener {
                            Log.e("SettingsActivity",it.toString())
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
        database.reference.child("NotificationsTL").child(userId).removeValue()
        database.reference.child("NotificationsConsultas").equalTo("uid",userId).ref.removeValue()
    }


    private fun deleteUserConsultas(userId: String) {
        val arrayTemas = arrayListOf("Embarazo","Familia","Parto","Posparto","Otros")
        for(tema in arrayTemas) {
            firestore.collection("Consultas").document(tema).collection("Consultas").whereEqualTo("userId",userId).addSnapshotListener { value, error ->
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
                }
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
        firebaseStore.getReference("Usuarios/$userId/images/perfil").delete().addOnSuccessListener {
        }.addOnFailureListener {
            Log.e("SettingsActivity",it.toString())
        }
        firebaseStore.getReference("Usuarios/$userId/images/layout").delete().addOnSuccessListener {
        }.addOnFailureListener {
            Log.e("SettingsActivity",it.toString())
        }
    }

    /**
     * Este método elimina los posts a los que ha dado 'like' el usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserLikes(userId: String) {
        firestore.collection("Likes").document(userId).collection("Likes").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                firestore.collection("Timeline").document(it.id).collection("Likes").document(userId).delete()
                it.reference.delete()
            }
        }

    }

    /**
     * Este método elimina los amigos del usuario.
     * @param userId String : UID del usuario
     */
    private fun deleteUserFriends(userId: String){
        var reference = firestore.collection("Friendship")
        reference.document(userId).collection("Friends").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                reference.document(it.id).collection("Friends").document(userId).delete()
                it.reference.delete()
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

            img_user.setOnClickListener {
                val intent = Intent(context, PerfilActivity::class.java)
                intent.putExtra("UserUID", uid)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

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

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var txt_nombre_user: TextView = itemView.findViewById(R.id.txt_nombre_amigo)
        var txt_username_user: TextView = itemView.findViewById(R.id.txt_user_amigo)
        var img_user: ImageView = itemView.findViewById(R.id.img_amigos)
        var btn_menu_user: Button = itemView.findViewById(R.id.btn_menu_friends)
        var verified: ImageView = itemView.findViewById(R.id.verified)
    }

}