package com.example.heymama.activities

import android.media.Image
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
import com.example.heymama.adapters.PostTimelineAdapter
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.PostTimeline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.w3c.dom.Text
import java.util.*

class ChatActivity : AppCompatActivity(), ItemRecyclerViewListener {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var uid: String
    lateinit var friendUID: String
    lateinit var txt_message_chat: EditText

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsArraylist: ArrayList<Message>
    private lateinit var adapterChats: ChatAdapter

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
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference


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
        recyclerViewChats.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL,false)
        recyclerViewChats.setHasFixedSize(true)
        chatsArraylist = arrayListOf()

        getMessage(auth.uid.toString(),friendUID)
        updateFriendName()
        updatePicture()

    }

    private fun sendMessage(senderUID: String, receiverUID:String, msg: String) {
        var message = Message(senderUID,receiverUID,msg, Date()) //senderUID+"_"+receiverUID
        firestore.collection("Chats").document(auth.uid.toString()).collection("Chat").add(message).addOnCompleteListener {
            if (it.isSuccessful) {
                firestore.collection("Chats").document(receiverUID).collection("Chat").add(message)
                txt_message_chat.setText("")
                Toast.makeText(this,"El mensaje se ha enviado correctamente",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"No se ha podido enviar el mensaje",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun getMessage(senderUID: String, receiverUID: String) {

        firestore.collection("Chats").document(auth.uid.toString()).collection("Chat").addSnapshotListener { snapshots, error ->
            if (error != null) {
                return@addSnapshotListener
            } else {
                for (dc in snapshots!!.documentChanges) {

                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val message = dc.document.toObject(Message::class.java)
                            if((message.senderUID == senderUID && message.receiverUID == receiverUID) ||
                                (message.senderUID == receiverUID && message.receiverUID == senderUID)){
                                chatsArraylist.add(message)
                            }
                        }
                        DocumentChange.Type.MODIFIED -> chatsArraylist.add(dc.document.toObject(
                            Message::class.java))
                        DocumentChange.Type.REMOVED -> chatsArraylist.remove(dc.document.toObject(
                            Message::class.java))
                    }
                    adapterChats = ChatAdapter(this,chatsArraylist,this)
                    recyclerViewChats.adapter = adapterChats
                }
            }
        }

    }

    private fun updateFriendName(){
        var txt_chat_friend_name = findViewById<TextView>(R.id.txt_chat_friend_name)
        firestore.collection("Usuarios").document(friendUID).get().addOnSuccessListener {
            txt_chat_friend_name.text = it.get("name").toString()
        }
    }

    private fun updatePicture(){
        var imageView_chat = findViewById<ImageView>(R.id.imageView_chat)
        glidePicture(friendUID,imageView_chat)
    }

    private fun glidePicture(uid: String, image: ImageView) {
        storageReference = firebaseStore.getReference("/Usuarios/"+uid+"/images/perfil")

        GlideApp.with(applicationContext)
            .load(storageReference)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(image)
    }
}
