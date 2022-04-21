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
import com.example.heymama.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendRequestAdapter(private val context: Context, private val friendRequestList: ArrayList<FriendRequest>
) : RecyclerView.Adapter<FriendRequestAdapter.HolderForo>() {
    // FirebaseAuth object
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
     *
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendRequestAdapter.HolderForo {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.tema_friendrequests, parent, false)
        return HolderForo(view)
    }

    /**
     *
     * @param holder FriendRequestAdapter.HolderForo
     * @param position Int
     *
     */
    override fun onBindViewHolder(holder: FriendRequestAdapter.HolderForo, position: Int) {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStore.reference
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")

        dataBaseReference = dataBase.getReference("Usuarios")

        val friendRequest = friendRequestList[position].friend_send_uid //BUSCA EL USUARIO QUE ENVIÓ LA SOLICITUD

        dataBaseReference.child(friendRequest).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.txt_nombre_solicitud.text = snapshot.child("name").value.toString()
                holder.txt_user_solicitud.text = snapshot.child("user").value.toString()
                Log.i("Adapter-3",snapshot.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("ERROR","error-friend")
            }
        })


        storageReference = firebaseStore.getReference("/Usuarios/"+friendRequest+"/images/perfil")
        val ONE_MEGABYTE: Long = 1024 * 1024
        storageReference
            .getBytes(8 * ONE_MEGABYTE).
            addOnSuccessListener { bytes ->
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.img_solicitud.setImageBitmap(bmp)
            }.addOnFailureListener {
                Log.e(ContentValues.TAG, "Se produjo un error al descargar la imagen.", it)
            }

        // BOTÓN ACEPTAR SOLICITUD
        var acceptButton: Button = holder.btn_aceptar_solicitud
        acceptButton.setOnClickListener {
            acceptFriendRequest(holder)
        }

        // BOTÓN RECHAZAR SOLICITUD
        var denyButton: Button = holder.btn_rechazar_solicitud
        denyButton.setOnClickListener {
            denyFriendRequest(holder)
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

            friends = FriendRequest(id, auth.currentUser?.uid.toString(), "friends")
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
     *
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
     *
     */
    private fun searchFriendRequest(id: String, request: String) {
        firestore.collection("Friendship").document(auth.currentUser?.uid.toString()).collection("FriendRequest")
            .document(id).addSnapshotListener { value, error ->
                if(value != null) {
                    var friendRequest = value.toObject(FriendRequest::class.java)

                    updateFriendRequest(id,request)

                    for(item in friendRequestList) {
                        var friend_receive = item.friend_receive_uid
                        var friend_send = item.friend_send_uid
                        var state = item.state
                        if (friendRequest != null) {
                            if ((friend_receive.equals(friendRequest?.friend_receive_uid)) && (friend_send.equals(friendRequest.friend_send_uid))&& (state.equals(friendRequest.state))) {
                                friendRequestList.remove(item)
                                notifyDataSetChanged()
                            } else {
                                Log.i("ACCEPT1","a: "+ friend_receive + " " + friend_send + " " + state)
                                Log.i("ACCEPT1","c: "+ friendRequest!!.friend_receive_uid + " " + friendRequest.friend_send_uid + " " +friendRequest.state)
                            }
                        }
                    }
                }
            }
    }

    /**
     *
     * @param holder FriendRequestAdapter.HolderForo
     *
     */
    private fun acceptFriendRequest(holder: FriendRequestAdapter.HolderForo) {
        Log.i("ACCESS1",holder.txt_user_solicitud.text.toString())
        var holder_username = holder.txt_user_solicitud.text.toString()
        //BUSCA EL ID DEL USERNAME CAPTURADO EN EL HOLDER
        firestore.collection("Usuarios").addSnapshotListener { value, error ->
            if(value != null) {
                var documents = value.documents
                documents.forEach { d ->
                    //NO FUNCIONA CON SOLICITUD DE PROFESIONAL PORQUE LOS ATRIBUTOS DEL REGISTRO ESTAN CON MINUSCULAS
                    if(d.data?.get("username").toString().equals(holder_username)) {
                        var id = d.data?.get("ID").toString()
                        searchFriendRequest(id,"aceptar")
                    }
                }
            }
        }
        /*firestore.collection("Usuarios").document(auth.currentUser?.uid.toString()).get().addOnSuccessListener(
            OnSuccessListener<DocumentSnapshot> { documentSnapshot ->
                var id = documentSnapshot.get("ID").toString()
                searchFriendRequest(id)
            })
          */
    }

    /**
     *
     * @param holder FriendRequesAdapter.HolderForo
     *
     */
    private fun denyFriendRequest(holder:FriendRequestAdapter.HolderForo) {
        var holder_username = holder.txt_user_solicitud.text.toString()
        //BUSCA EL ID DEL USERNAME CAPTURADO EN EL HOLDER
        firestore.collection("Usuarios").addSnapshotListener { value, error ->
            if(value != null) {
                var documents = value.documents
                documents.forEach { d ->
                    //NO FUNCIONA CON SOLICITUD DE PROFESIONAL PORQUE LOS ATRIBUTOS DEL REGISTRO ESTAN CON MINUSCULAS
                    if(d.data?.get("Username").toString().equals(holder_username)) {
                        var id = d.data?.get("ID").toString()
                        searchFriendRequest(id,"rechazar")
                    }
                }
            }
        }
    }

    /**
     *
     * @param input
     *
     */
    override fun getItemCount(): Int {
        return friendRequestList.size
    }

    /**
     *
     * @param itemView View
     *
     */
    inner class HolderForo(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_nombre_solicitud: TextView = itemView.findViewById(R.id.txt_nombre_solicitud)
        var txt_user_solicitud: TextView = itemView.findViewById(R.id.txt_user_solicitud)
        var img_solicitud: ImageView = itemView.findViewById(R.id.img_solicitud)
        var btn_aceptar_solicitud: Button = itemView.findViewById(R.id.btn_aceptar_solicitud)
        var btn_rechazar_solicitud: Button = itemView.findViewById(R.id.btn_rechazar_solicitud)
    }
}

