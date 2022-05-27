package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.adapters.NotificationsAdapter
import com.example.heymama.databinding.ActivityNotificationsBinding
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.Consulta
import com.example.heymama.models.Notification
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class NotificationsActivity : AppCompatActivity(), ItemRecyclerViewListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerViewNotifications: RecyclerView
    private lateinit var notificationsArraylist: ArrayList<Notification>
    private lateinit var adapterNotifications: NotificationsAdapter
    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.uid.toString()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initRecycler()
        //getNotificationsTL()
        getUserData()
    }

    private fun initRecycler() {
        recyclerViewNotifications = binding.recyclerViewNotifications
        recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        notificationsArraylist = arrayListOf()
        adapterNotifications = NotificationsAdapter(this,notificationsArraylist,this)
        recyclerViewNotifications.adapter = adapterNotifications
    }

    private fun getUserData() {
        database.reference.child("Usuarios").child(uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if(user!!.rol == "Profesional") {
                        getNotificationsTL()
                        getNotificationsConsultas()
                    } else {
                        getNotificationsTL()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Este método permite obtener las notificaciones de los comentarios recibidos en posts publicados
     * por el usuario en la timeline.
     */
    private fun getNotificationsTL() {
        notificationsArraylist.clear()
        val ref = database.reference.child("NotificationsTL").child(uid)
        ref.get().addOnSuccessListener { it ->
            if(it.exists()) {
                it.children.iterator().forEach {
                    val notification = it.getValue(Notification::class.java)
                    if(notification!!.uid != auth.uid.toString()) {
                        firestore.collection("Timeline").document(notification.idpost.toString()).collection("Replies")
                            .whereEqualTo("comment",notification.textpost).addSnapshotListener { value, error ->
                                value!!.documents.iterator().forEach {
                                    if(it.exists()) {
                                        notificationsArraylist.add(notification)
                                    }
                                }
                            }
                    }
                    adapterNotifications.notifyDataSetChanged()
                    adapterNotifications.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                        override fun onItemClicked(position: Int) {
                        }
                    })
                }
            }
        }

    }

    /**
     * Este método permite obtener las notificaciones de las consultas recibidas.
     */
    private fun getNotificationsConsultas() {
        //notificationsArraylist.clear()
        database.reference.child("NotificationsConsultas").get().addOnSuccessListener { it ->
            if(it.exists()) {
                it.children.iterator().forEach {
                    val notification = it.getValue(Notification::class.java)
                    val idconsulta = notification!!.idpost
                    firestore.collection("Consultas").addSnapshotListener { value, error ->
                        value!!.documents.iterator().forEach {
                            it.reference.collection("Consultas").whereEqualTo("id",idconsulta).addSnapshotListener { value, error ->
                                value!!.documents.iterator().forEach {
                                    val consulta = it.toObject(Consulta::class.java)
                                    if(consulta != null) {
                                        notificationsArraylist.add(notification!!)
                                        adapterNotifications.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}