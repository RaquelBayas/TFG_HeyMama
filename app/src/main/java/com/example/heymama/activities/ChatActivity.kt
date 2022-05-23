package com.example.heymama.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.example.heymama.models.Message
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.*
import com.example.heymama.R
import com.example.heymama.adapters.ChatAdapter
import com.example.heymama.databinding.ActivityChatBinding
import com.example.heymama.interfaces.APIService
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.example.heymama.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorageRef: StorageReference
    private lateinit var database: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var uid: String
    private lateinit var friendUID: String
    private lateinit var txt_message_chat: EditText
    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsArraylist: ArrayList<Message>
    private lateinit var adapterChats: ChatAdapter
    private lateinit var binding: ActivityChatBinding
    private var notify = false
    private var apiService: APIService?=null

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        database = FirebaseDatabase.getInstance()
        dataBaseReference = database.getReference("Usuarios")
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseStorageRef = FirebaseStorage.getInstance().reference

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        val intent = intent
        friendUID = intent.getStringExtra("friendUID").toString()

       binding.txtChatFriendName.text = friendUID


        binding.imgBackChat.setOnClickListener {
            onBackPressed()
        }

        txt_message_chat = binding.txtMessageChat

        binding.btnSendMessageChat.setOnClickListener {
            if(txt_message_chat.text.isEmpty()){
                Toast.makeText(this,"Introduce un mensaje",Toast.LENGTH_SHORT).show()
            } else {
                notify=true
                val imagePath = ""
                val message = txt_message_chat.text.toString()
                sendMessageImage(auth.uid.toString(),friendUID,message,imagePath)
            }
        }

        recyclerViewChats = binding.recyclerViewChat
        chatsArraylist = arrayListOf()
        adapterChats = ChatAdapter(applicationContext, chatsArraylist,this)
        adapterChats.setOnItemRecyclerViewListener(object: ItemRecyclerViewListener {
            override fun onItemLongClicked(position: Int) {
                deleteMsg(chatsArraylist[position])
            }
        })

        val linearLayoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true)
        linearLayoutManager.stackFromEnd = false

        recyclerViewChats.layoutManager = linearLayoutManager
        recyclerViewChats.recycledViewPool.setMaxRecycledViews(0,0)
        recyclerViewChats.smoothScrollToPosition(adapterChats.itemCount)
        recyclerViewChats.scrollToPosition(adapterChats.itemCount-1)
        recyclerViewChats.setHasFixedSize(false)
        recyclerViewChats.adapter = adapterChats

        getMessage(auth.uid.toString(),friendUID)
        updateFriendName()
        updatePicture()
        checkStatus()
        binding.btnAddImgChat.setOnClickListener {
            selectImage(100)
        }
    }

    private fun checkStatus() {
        database.getReference("Usuarios/$friendUID").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.iterator().forEach {

                        Log.i("CHECK-STATUS-2",it.toString())
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun deleteMsg(message: Message) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.eliminar)
            .setMessage(R.string.alert_eliminar)
            .setNegativeButton("Cancelar") { view, _ ->
                Toast.makeText(this, "Cancel button pressed", Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setPositiveButton("Eliminar") { view, _ ->
                chatsArraylist.remove(message)
                var user2 = message.receiverUID
                if(user2 == auth.uid.toString()) user2 = message.senderUID

                val ref = database.reference.child("Chats").child(auth.uid.toString()).child("Messages").child(user2)
                ref.orderByChild("timestamp").addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot)  {
                        val listMsgs = arrayListOf<Message>()
                        for((position,it) in snapshot.children.iterator().withIndex()){
                            if(it.getValue(Message::class.java) == message && !it.key.equals("LastMessage")) {
                                ref.child(it.key.toString()).removeValue()
                                if(listMsgs.size >= 1) {
                                    val lastMessage: Map<String, Message> = mapOf("LastMessage" to listMsgs[listMsgs.size - 1])
                                    ref.updateChildren(lastMessage)
                                } else {
                                    ref.child("LastMessage").removeValue()
                                }
                            } else {
                                listMsgs.add(it.getValue(Message::class.java)!!)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                adapterChats.notifyDataSetChanged()
                view.dismiss()
            }
            .setCancelable(false)
            .create()
        dialog.show()
    }

    /**
     *
     * @param code Int
     */
    private fun selectImage(code: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent,code)
    }

    /**
     *
     * @param storageReference StorageReference
     * @param uri Uri
     */
    private fun uploadImage(storageReference: StorageReference, uri: Uri){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Subiendo foto...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.putFile(uri).addOnCompleteListener{ task ->
            progressDialog.dismiss()
            if(task.isSuccessful) {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imagePath = uri.toString()
                    val message = ""//Message(auth.uid.toString(),friendUID,"",imagePath,Date().time)
                    notify=true
                    sendMessageImage(auth.uid.toString(),friendUID,message,imagePath)
                }
            }
        }

        storageReference.putFile(uri).
        addOnSuccessListener {
            Toast.makeText(this,"Foto subida",Toast.LENGTH_SHORT).show()
            if(progressDialog.isShowing) progressDialog.dismiss()
        }.addOnFailureListener{
            if(progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(this,"Hubo un error",Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * //Está ligado a selectImage
     *
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode == 100) && (resultCode == Activity.RESULT_OK)) {
            if(data != null) {
                if(data.data != null) {
                    val selectedImage = data.data
                    val date = Calendar.getInstance()
                    val reference = firebaseStorage.getReference("Chats/"+auth.uid.toString()+friendUID+"/"+date.timeInMillis.toString()) //child("Chats").child(date.timeInMillis.toString())
                    uploadImage(reference, selectedImage!!)
                }
            }
        }
    }

    private fun sendMessageImage(senderUID: String, receiverUID: String, txtMessage: String, imagePath: String) {

        val ref = database.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID).push()
        val message = Message(ref.key.toString(),senderUID,receiverUID,txtMessage,imagePath,Date().time)
        ref.setValue(message).addOnSuccessListener {
            database.reference.child("Chats").child(receiverUID).child("Messages")
                .child(senderUID).push().setValue(message)
                .addOnSuccessListener {
                    binding.txtMessageChat.setText("")
                    val chatListRef = database.reference.child("ChatList")
                        .child(senderUID)
                        .child(receiverUID)
                    chatListRef.addListenerForSingleValueEvent(object:ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()) {
                                chatListRef.child("id").setValue(receiverUID)
                            }
                            val chatListReceiverRef = database.reference.child("ChatList")
                                .child(receiverUID)
                                .child(senderUID)

                            chatListReceiverRef.child("id").setValue(senderUID)
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
        }
        val lastMessage : Map<String, Message> = mapOf("LastMessage" to message)
        database.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID)
            .updateChildren(lastMessage)
            .addOnSuccessListener {
                database.reference.child("Chats").child(receiverUID).child("Messages")
                    .child(senderUID)
                    .updateChildren(lastMessage)
                    .addOnSuccessListener {
                        binding.txtMessageChat.setText("")
                        Toast.makeText(applicationContext,
                            "El mensaje se ha enviado correctamente",
                            Toast.LENGTH_SHORT).show()
                    }
            }

        var refNotify = database.reference.child("Usuarios").child(uid)
        refNotify.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (notify) {
                    if(imagePath=="") {
                        sendNotification(friendUID, user!!.name, txtMessage)
                    }else {
                        sendNotification(friendUID,user!!.name,"Te ha enviado una imagen")
                    }
                }
                notify=false
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children) {
                    val token: Token?=dataSnapshot.getValue(Token::class.java)
                    val data = Data(uid,R.drawable.logoapp,"$userName: $message","New Message",friendUID)
                    val sender = Sender(data,token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        ?.enqueue(object : Callback<MyResponse?> {
                            override fun onResponse(call: Call<MyResponse?>, response: Response<MyResponse?>) {
                                if(response.code()==200) {
                                    if(response.body()!!.success!==1) {
                                        Toast.makeText(this@ChatActivity,"Failed, Nothing happened.",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            override fun onFailure(call: Call<MyResponse?>, t: Throwable) {
                            }
                        })
                }
            }
            override fun onCancelled(p0: DatabaseError) {
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
        database.reference.child("Chats").child(senderUID).child("Messages").child(receiverUID).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArraylist.clear()
                for (datasnapshot: DataSnapshot in snapshot.children) {
                    if(!datasnapshot.key.equals("LastMessage")) {
                        var message = datasnapshot.getValue(Message::class.java)
                        chatsArraylist.add(message!!)
                    }
                }
                if(chatsArraylist.size>1) {
                    chatsArraylist.sort()
                }
                adapterChats.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     *
     *
     */
    private fun updateFriendName(){
        val txt_chat_friend_name = findViewById<TextView>(R.id.txt_chat_friend_name)
        firestore.collection("Usuarios").document(friendUID).get().addOnSuccessListener {
            txt_chat_friend_name.text = it.get("name").toString()
        }
    }

    /**
     *
     */
    private fun updatePicture(){
        val imageView_chat = findViewById<ImageView>(R.id.imageView_chat)
        glidePicture(friendUID,imageView_chat)
    }

    /**
     *
     * @param uid String
     * @param image ImageView
     *
     */
    private fun glidePicture(uid: String, image: ImageView) {
        firebaseStorageRef = firebaseStorage.getReference("/Usuarios/$uid/images/perfil")
        GlideApp.with(applicationContext)
                .load(firebaseStorageRef)
                .error(R.drawable.wallpaper_profile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(image)
    }

    private fun currentUser(userid: String?) {
        val editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit()
        editor.putString("currentUser", userid)
        editor.apply()
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        Utils.updateStatus("offline")
        currentUser(uid)
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        Utils.updateStatus("online")
        currentUser(uid)
    }
}
