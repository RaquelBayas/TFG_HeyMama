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
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserAdapter(private val context: Context, private var usersList: ArrayList<User>, private var uid: String) : RecyclerView.Adapter<UserAdapter.Holder>() {
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var uidFriend: String
    private val ONE_MEGABYTE : Long = 1024 * 1024

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_nombre_amigo: TextView = itemView.findViewById(R.id.txt_nombre_amigo)
        var txt_user_amigo: TextView = itemView.findViewById(R.id.txt_user_amigo)
        var img_amigos: ImageView = itemView.findViewById(R.id.img_amigos)
        var btn_menu_friends: Button = itemView.findViewById(R.id.btn_menu_friends)
    }

    fun filterList(list: ArrayList<User>) {
        this.usersList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend,parent,false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference

        with(holder) {
            txt_nombre_amigo.text = usersList[position].name
            txt_user_amigo.text = usersList[position].username

            storageReference = firebaseStore.getReference("/Usuarios/"+usersList[position].id+"/images/perfil")
            storageReference
                .getBytes(8 * ONE_MEGABYTE).
                addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    img_amigos.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                }
            Log.i("USERADAPTER",storageReference.path)
            img_amigos.setOnClickListener {
                visitFriend(usersList[position].id.toString())
            }
        }

        menuFriend(holder)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    private fun visitFriend(uid:String) {
        val intent = Intent(context, PerfilActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("UserUID",uid)
        this.context.startActivity(intent)
    }

    private fun menuFriend(holder: UserAdapter.Holder) {
        with(holder) {
            if (uid == auth.uid) {
                btn_menu_friends.visibility = View.VISIBLE
                btn_menu_friends.setOnClickListener {
                    val popupMenu: PopupMenu = PopupMenu(context, holder.btn_menu_friends)
                    popupMenu.menuInflater.inflate(R.menu.post_tl_menu,
                        popupMenu.menu) //Sólo tiene la opción de eliminar
                    popupMenu.show()
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.eliminar_post_tl -> {
                                getUIDFriend(txt_user_amigo.text.toString())
                            }
                        }
                        true
                    })
                }
            }
        }
    }

    /**
     *
     * @param username String
     */
    private fun getUIDFriend(username: String) {
        firestore.collection("Usuarios").whereEqualTo("username",username).get().addOnSuccessListener {
            for (doc in it.documents) {
                uidFriend = doc["ID"].toString()
                removeFriend(uidFriend)
            }
        }
    }

    /**
     *
     * @param uidFriend String
     *
     */
    private fun removeFriend(uidFriend: String) {
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").document(uidFriend).delete().addOnSuccessListener{
            firestore.collection("Friendship").document(uidFriend).collection("Friends").document(auth.uid.toString()).delete()
        }.addOnFailureListener {
            Log.i("FriendsAdapter", "Se ha producido un error.")
        }
    }
}