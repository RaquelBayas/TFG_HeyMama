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
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.User
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ListaUsuariosAdapter(private val context: Context, private var listaUsuarios: ArrayList<User>
) : RecyclerView.Adapter<ListaUsuariosAdapter.Holder>() {

    private lateinit var listener: ItemRecyclerViewListener
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
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")

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
           deleteAuth(user)
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
        deleteUserPosts(userId)
        deleteUserMood(userId)
        deleteUserForos(userId)
        deleteUserConsultas(userId)
        if(user.rol == "Profesional"){
            deleteUserArticulos(userId)
        }
        deleteUserPhotos(userId)
        deleteUserLikes(userId)
        deleteUserFriends(userId)
    }

    private fun deleteAuth(user: User) {


    }

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
                        Log.i("listusers",posts.reference.path)
                        posts.reference.delete()
                    }
                }
            }
        }
    }

    private fun deleteUserMood(userId: String) {
        firestore.collection("Mood").document(userId).collection("Historial").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                it.reference.delete()
            }
        }
    }

    private fun deleteUserForos(userId: String){
        var arrayForos = arrayListOf<String>("Depresión","Posparto","Embarazo","Otros")
        for(foro in arrayForos) {
            firestore.collection("Foros").document("SubForos").collection(foro).whereEqualTo("userID",userId).addSnapshotListener { value, error ->
                value!!.documents.iterator().forEach {
                    it.reference.collection("Comentarios").addSnapshotListener { value, error ->
                        value!!.documents.iterator().forEach { it.reference.delete() }
                    }
                    it.reference.delete()
                }
            }
        }
    }

    private fun deleteUserConsultas(userId: String) {
        var arrayTemas = arrayListOf<String>("Embarazo","Familia","Parto","Posparto","Otros")
        for(tema in arrayTemas) {
            firestore.collection("Consultas").document(tema).collection("Consultas").addSnapshotListener { value, error ->
                value!!.documents.iterator().forEach {
                    it.reference.collection("Respuestas").addSnapshotListener { value, error ->
                        value!!.documents.iterator().forEach { it.reference.delete() }
                    }
                    it.reference.delete()
                }
            }
        }
    }

    private fun deleteUserArticulos(userId: String) {
        firestore.collection("Artículos").whereEqualTo("professionalID",userId).addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach { it.reference.delete() }
        }
    }

    private fun deleteUserPhotos(userId: String) {
        firebaseStore.getReference("Usuarios/"+userId+"/images/perfil").delete()
        firebaseStore.getReference("Usuarios/"+userId+"/images/layout").delete()
    }

    private fun deleteUserLikes(userId: String) {
        firestore.collection("Likes").document(userId).collection("Likes").addSnapshotListener { value, error ->
            value!!.documents.iterator().forEach {
                firestore.collection("Timeline").document(it.id).collection("Likes").document(userId).delete()
                it.reference.delete()
            }
        }

    }

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
            Log.i("url-user",storageReference.toString())
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