package com.example.heymama.adapters

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ListaUsuariosAdapter(private val context: Context, private val listaUsuarios: ArrayList<User>
) : RecyclerView.Adapter<ListaUsuariosAdapter.Holder>() {

    private lateinit var listener: ItemRecyclerViewListener
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaUsuariosAdapter.Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friend,parent,false)
        return ListaUsuariosAdapter.Holder(view)
    }

    override fun onBindViewHolder(holder: ListaUsuariosAdapter.Holder, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")

        dataBaseReference = dataBase.getReference("Usuarios")

        getUsuarios(holder,position)
    }

    private fun getUsuarios(holder: ListaUsuariosAdapter.Holder, position: Int) {
        var user = listaUsuarios[position]
        with(holder) {
            txt_nombre_user.text = user.name
            txt_username_user.text = user.username
            var uid = user.id
            storageReference = firebaseStore.getReference("/Usuarios/"+uid+"/images/perfil")
            val ONE_MEGABYTE: Long = 1024 * 1024
            storageReference
                .getBytes(8 * ONE_MEGABYTE).
                addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    img_user.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
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
    }

}