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
import com.example.heymama.R
import com.example.heymama.activities.PerfilActivity
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendsAdapter(private val context: Context, private val friendsList: ArrayList<FriendRequest>
) : RecyclerView.Adapter<FriendsAdapter.HolderForo>() {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int
    ): FriendsAdapter.HolderForo {

        // inflate layout tema_friend.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend, parent, false)
        return HolderForo(view)
    }

    override fun onBindViewHolder(holder: HolderForo, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")

        dataBaseReference = dataBase.getReference("Usuarios")

        getFriends(holder,position)

    }

    private fun getFriends(holder:HolderForo,position:Int) {

        var uid = friendsList[position].friend_send_uid

        firestore.collection("Usuarios").document(uid).addSnapshotListener { value, error ->
            holder.txt_nombre_amigo.text = value?.data?.get("name").toString() // MAYUSCULA?
            holder.txt_user_amigo.text = value?.data?.get("username").toString()

            storageReference = firebaseStore.getReference("/Usuarios/"+uid+"/images/perfil")
            val ONE_MEGABYTE: Long = 1024 * 1024
            storageReference
                .getBytes(8 * ONE_MEGABYTE).
                addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.img_amigos.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
                }
        }
        holder.img_amigos.setOnClickListener {
            visitFriend(uid)
        }
    }

    private fun visitFriend(uid:String) {
        val intent = Intent(context, PerfilActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("UserUID",uid)
        this.context.startActivity(intent)

    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_nombre_amigo: TextView = itemView.findViewById(R.id.txt_nombre_amigo)
        var txt_user_amigo: TextView = itemView.findViewById(R.id.txt_user_amigo)
        var img_amigos: ImageView = itemView.findViewById(R.id.img_amigos)
    }

}