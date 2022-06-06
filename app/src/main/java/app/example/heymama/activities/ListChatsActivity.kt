package app.example.heymama.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.example.heymama.Token
import app.example.heymama.Utils
import app.example.heymama.adapters.ListChatItemAdapter
import app.example.heymama.databinding.ActivityListChatsBinding
import app.example.heymama.interfaces.ItemRecyclerViewListener
import app.example.heymama.models.ListChat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class ListChatsActivity : AppCompatActivity(), ItemRecyclerViewListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsArraylist: ArrayList<ListChat>
    private lateinit var adapterChats: ListChatItemAdapter
    private lateinit var binding: ActivityListChatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        initRecycler()

        binding.swipeRefreshTL.setOnRefreshListener {
            setChats()
        }
        setChats()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            updateToken(token.toString())
        }
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        dataBase = FirebaseDatabase.getInstance()
    }

    private fun initRecycler() {
        recyclerViewChats = binding.recyclerViewListChats
        recyclerViewChats.layoutManager = LinearLayoutManager(this)
        recyclerViewChats.recycledViewPool.setMaxRecycledViews(0,0)
        recyclerViewChats.setHasFixedSize(true)
        chatsArraylist = arrayListOf()
        adapterChats = ListChatItemAdapter(applicationContext, chatsArraylist)
        recyclerViewChats.adapter = adapterChats
    }

    private fun updateToken(token: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(auth.uid.toString()).setValue(token1)
    }

    private fun setChats() {
        chatsArraylist.clear()
        if(binding.swipeRefreshTL.isRefreshing) {
            binding.swipeRefreshTL.isRefreshing = false
        }
        var ref = dataBase.reference.child("ChatList").child(auth.uid.toString())
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArraylist.clear()
                if(snapshot.exists()) {
                    for (snapshot in snapshot.children) {
                        val chatlist = snapshot.getValue(ListChat::class.java)
                        chatsArraylist.add(chatlist!!)
                        adapterChats.setOnItemRecyclerViewListener(object :
                            ItemRecyclerViewListener {
                            override fun onItemClicked(position: Int) {
                                val intent = Intent(applicationContext, ChatActivity::class.java)
                                intent.putExtra("friendUID", chatsArraylist[position].id)
                                startActivity(intent)
                            }
                        })
                    }
                    adapterChats.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        Utils.updateStatus("offline")
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        Utils.updateStatus("online")
    }
}