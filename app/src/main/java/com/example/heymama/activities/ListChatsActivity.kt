package com.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ListChatItemAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.ListChatItem
import com.example.heymama.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

class ListChatsActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsArraylist: ArrayList<ListChatItem>
    private lateinit var adapterChats: ListChatItemAdapter

    private lateinit var receiver_name: String
    private lateinit var receiver_username: String
    private lateinit var idUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_chats)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        recyclerViewChats = findViewById(R.id.recyclerView_listChats)
        recyclerViewChats.layoutManager = LinearLayoutManager(this)
        recyclerViewChats.setHasFixedSize(true)

        chatsArraylist = arrayListOf()
        adapterChats = ListChatItemAdapter(applicationContext, chatsArraylist, this)
        adapterChats.notifyDataSetChanged()
        setChats()

    }

    private fun setChats() {
    //var ref_two = ref_one.collection("Chats").document("3nLuKE4icCVSzvGrMbD0xukDs5l1").collection("Chats").orderBy("timestamp",Query.Direction.DESCENDING).limit(1)
        var ref = dataBase.reference.child("Chats").child(auth.uid.toString()).child("Messages")
        ref.addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArraylist.clear()
                for(datasnapshot in snapshot.children) {
                    for(datasn in datasnapshot.children) {
                        if(datasn.key.equals("LastMessage")) {
                            var msg: Message? = datasn.getValue(Message::class.java)
                            var userUid = msg!!.receiverUID
                            if(userUid.equals(auth.uid.toString())) {
                                userUid = msg!!.senderUID
                            }
                            getUserData(userUid, msg, datasn)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     *
     */
    private fun getUserData(userUid: String, msg: Message, datasn: DataSnapshot) {
        /*dataBase.reference.child("Usuarios").child(userUid).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var user = snapshot.getValue(User::class.java)
                receiver_name = user.name
                receiver_username = user.user
                Log.i("CHATUSERNAME",snapshot.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })*/
        var ref = firestore.collection("Usuarios").document(userUid)
        ref.addSnapshotListener { value, error ->
            idUser = value!!.data!!.get("ID").toString()
            receiver_name = value!!.data!!.get("name").toString()
            receiver_username = value!!.data!!.get("username").toString()

            var chatItem = ListChatItem(datasn.key.toString(), idUser, receiver_name, receiver_username, msg.message, Date())
            chatsArraylist.clear()
            chatsArraylist.add(chatItem)

            recyclerViewChats.adapter = adapterChats
            adapterChats.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
                override fun onItemClicked(position: Int) {
                    val intent = Intent(applicationContext, ChatActivity::class.java)
                    intent.putExtra("friendUID", chatsArraylist[position].idUser)
                    startActivity(intent)
                    Toast.makeText(this@ListChatsActivity,"Item number: $position", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}