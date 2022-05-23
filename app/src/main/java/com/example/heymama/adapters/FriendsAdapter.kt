package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.models.FriendRequest
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendsAdapter(private val context: Context, private var friendsList: ArrayList<FriendRequest>, private val uidProfileFriends : String
) : RecyclerView.Adapter<FriendsAdapter.Holder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var uidFriend: String
    private lateinit var uid: String
    private val ONE_MEGABYTE : Long = 1024 * 1024

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = firebaseStore.reference
        getFriends(holder,position)
    }

    /**
     * Este método permite obtener cada uno de los amigos que hemos agregado.
     * @param holder Holder
     * @param position Int
     */
    private fun getFriends(holder:Holder,position:Int) {
        if(friendsList[position].friend_receive_uid != uidProfileFriends) {
            uid = friendsList[position].friend_receive_uid
        } else {
            uid = friendsList[position].friend_send_uid
        }

        with(holder) {
            firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
                val user = value?.toObject(User::class.java)
                txt_nombre_amigo.text = user!!.name
                txt_user_amigo.text = user!!.username

                storageReference = firebaseStore.getReference("/Usuarios/"+user.id+"/images/perfil")
                storageReference
                    .getBytes(8 * ONE_MEGABYTE).
                    addOnSuccessListener { bytes ->
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        img_amigos.setImageBitmap(bmp)
                    }.addOnFailureListener {
                        Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                    }
                img_amigos.setOnClickListener {
                    visitFriend(value?.data?.get("id").toString())
                }
            }
            menuFriend(holder)
        }
    }

    /**
     * Este método permite añadir un PopUpMenu con la opción 'eliminar' para eliminar la amistad con un usuario.
     * @param holder Holder
     */
    private fun menuFriend(holder: Holder) {
        with(holder) {
            if (uidProfileFriends == auth.uid) {
                btn_menu_friends.visibility = View.VISIBLE
                btn_menu_friends.setOnClickListener {
                    val popupMenu: PopupMenu = PopupMenu(context, holder.btn_menu_friends)
                    popupMenu.menuInflater.inflate(R.menu.post_tl_menu, popupMenu.menu)
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
     * Este método permite obtener el uid del usuario a partir de su nombre de usuario.
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
     * Este método permite eliminar a un usuario de nuestra lista de amigos.
     * @param uidFriend String : uid del usuario
     */
    private fun removeFriend(uidFriend: String) {
        firestore.collection("Friendship").document(auth.uid.toString()).collection("Friends").document(uidFriend).delete().addOnSuccessListener{
            firestore.collection("Friendship").document(uidFriend).collection("Friends").document(auth.uid.toString()).delete()
        }.addOnFailureListener {
            Log.i("FriendsAdapter", "Se ha producido un error.")
        }
    }

    /**
     * Este método permite acceder al perfil del amigo que hemos seleccionado.
     * @param uid String : uid del usuario
     */
    private fun visitFriend(uid:String) {
        val intent = Intent(context, PerfilActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("UserUID",uid)
        this.context.startActivity(intent)
    }

    /**
     * Devuelve la cantidad de elementos del arraylist "friendsList"
     */
    override fun getItemCount(): Int {
        return friendsList.size
    }

    /**
     * ViewHolder
     */
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_nombre_amigo: TextView = itemView.findViewById(R.id.txt_nombre_amigo)
        var txt_user_amigo: TextView = itemView.findViewById(R.id.txt_user_amigo)
        var img_amigos: ImageView = itemView.findViewById(R.id.img_amigos)
        var btn_menu_friends: Button = itemView.findViewById(R.id.btn_menu_friends)
    }



}