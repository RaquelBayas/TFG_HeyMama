package com.example.heymama.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.heymama.databinding.ActivityForosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.example.heymama.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ForosActivity : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var binding: ActivityForosBinding

    private lateinit var rol: String
    private lateinit var txt_depresion: TextView
    private lateinit var txt_embarazo: TextView
    private lateinit var txt_posparto: TextView
    private lateinit var txt_otros: TextView
    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")

        getDataUser()
        initForos()
    }

    /**
     * Este método permite inicializar los foros.
     */
    private fun initForos() {
        txt_depresion = binding.txtDepresion
        binding.txtDepresion.setOnClickListener{
            initForo(this,SubForoActivity::class.java,txt_depresion.text.toString())
        }

        txt_embarazo = binding.txtEmbarazo
        txt_embarazo.setOnClickListener{
            initForo(this,SubForoActivity::class.java,txt_embarazo.text.toString())
        }

        txt_posparto = binding.txtPosparto
        txt_posparto.setOnClickListener{
            initForo(this,SubForoActivity::class.java,txt_posparto.text.toString())
        }

        txt_otros = binding.txtOtros
        txt_otros.setOnClickListener {
            initForo(this,SubForoActivity::class.java,txt_otros.text.toString())
        }
    }

    /**
     * Este método permite obtener el rol del usuario
     */
    private fun getDataUser() {
        database.reference.child("Usuarios").child(auth.uid.toString())
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val user: User? = snapshot.getValue(User::class.java)
                        rol = user!!.rol.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    /**
     * Este método permite entrar en el foro seleccionado.
     * @param activity Activity
     * @param nameclass Class<*>
     * @param foroName String
     */
    private fun initForo(activity: Activity, nameclass: Class<*>?, foroName: String){
        val intent = Intent(activity, nameclass)
        intent.putExtra("ForoName",foroName)
        startActivity(intent)
    }

}