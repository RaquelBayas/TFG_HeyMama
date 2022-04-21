package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.heymama.models.Message
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.adapters.ChatAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ChatActivity : AppCompatActivity(), ItemRecyclerViewListener {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorageRef: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var friendUID: String
    private lateinit var txt_message_chat: EditText

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsArraylist: ArrayList<Message>
    private lateinit var adapterChats: ChatAdapter

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Usuario
        auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser
        uid = auth.currentUser?.uid!!
        // Firebase
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        dataBaseReference = dataBase.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStorage = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        firebaseStorageRef = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference


        val intent = intent
        friendUID = intent.getStringExtra("friendUID").toString()

        findViewById<TextView>(R.id.txt_chat_friend_name).text = friendUID

        // BOTÓN BACK
        var img_back_chat : ImageView = findViewById(R.id.img_back_chat)
        img_back_chat.setOnClickListener {
            onBackPressed()
        }

        txt_message_chat = findViewById(R.id.txt_message_chat)
        var btn_send_message: ImageView = findViewById(R.id.btn_send_message_chat)
        btn_send_message.setOnClickListener {
            if(txt_message_chat.text.isEmpty()){
                Toast.makeText(this,"Introduce un mensaje",Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(auth.uid.toString(),friendUID,txt_message_chat.text.toString())
            }
        }

        // RECYCLERVIEW TIMELINE
        recyclerViewChats = findViewById(R.id.recyclerView_chat)

        chatsArraylist = arrayListOf()
        adapterChats = ChatAdapter(applicationContext, chatsArraylist)
        recyclerViewChats.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL,false)
        recyclerViewChats.adapter = adapterChats
        recyclerViewChats.setHasFixedSize(true)

        (recyclerViewChats.layoutManager as LinearLayoutManager).stackFromEnd = true
        (recyclerViewChats.layoutManager as LinearLayoutManager).reverseLayout = true

        getMessage(auth.uid.toString(),friendUID)
        updateFriendName()
        updatePicture()
    }

    /**
     *
     * @param senderUID String
     * @param receiverUID String
     * @param msg String
     *
     */
    private fun sendMessage(senderUID: String, receiverUID: String, msg: String) {
        var message = Message(senderUID,receiverUID,msg, Date().time) //senderUID+"_"+receiverUID
        dataBase.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID).push()
            .setValue(message).addOnSuccessListener(object: OnSuccessListener<Void> {
                override fun onSuccess(p0: Void?) {
                    dataBase.reference.child("Chats").child(receiverUID).child("Messages").child(senderUID).push().setValue(message).addOnSuccessListener(object:OnSuccessListener<Void> {
                        override fun onSuccess(p0: Void?) {
                            txt_message_chat.setText("")
                            Toast.makeText(applicationContext,"El mensaje se ha enviado correctamente",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            })
        var lastMessage : Map<String, Message> = mapOf("LastMessage" to message)
        dataBase.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID)
            .updateChildren(lastMessage)
            .addOnSuccessListener(object: OnSuccessListener<Void> {
                override fun onSuccess(p0: Void?) {
                    dataBase.reference.child("Chats").child(receiverUID).child("Messages").child(senderUID)
                        .updateChildren(lastMessage).addOnSuccessListener(object:OnSuccessListener<Void> {
                        override fun onSuccess(p0: Void?) {
                            txt_message_chat.setText("")
                            Toast.makeText(applicationContext,"El mensaje se ha enviado correctamente",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            })
    }

    /**
     *
     * @param senderUID String
     * @param receiverUID String
     *
     */
    private fun getMessage(senderUID: String, receiverUID: String) {
        dataBase.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArraylist.clear()
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    if(!datasnapshot.key.equals("LastMessage")) {
                        var message = datasnapshot.getValue(Message::class.java)
                        chatsArraylist.add(message!!)
                    }
                }
                chatsArraylist.sort()
                adapterChats.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    /**
     *
     * @param input
     *
     */
    private fun updateFriendName(){
        var txt_chat_friend_name = findViewById<TextView>(R.id.txt_chat_friend_name)
        firestore.collection("Usuarios").document(friendUID).get().addOnSuccessListener {
            txt_chat_friend_name.text = it.get("name").toString()
        }
    }

    /**
     *
     * @param input
     *
     */
    private fun updatePicture(){
        var imageView_chat = findViewById<ImageView>(R.id.imageView_chat)
        glidePicture(friendUID,imageView_chat)
    }

    /**
     *
     * @param uid String
     * @param image ImageView
     *
     */
    private fun glidePicture(uid: String, image: ImageView) {
        firebaseStorageRef = firebaseStorage.getReference("/Usuarios/"+uid+"/images/perfil")

        GlideApp.with(applicationContext)
            .load(firebaseStorageRef)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }
}
