package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.heymama.R
import com.example.heymama.databinding.ActivityForosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ForosActivity : AppCompatActivity(), Utils{
    // FirebaseAuth object
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

        //Instancias para la base de datos y la autenticación
        database = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        // Usuario
        user = auth.currentUser!!
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")

        getDataUser()

        txt_depresion = binding.txtDepresion
        binding.txtDepresion.setOnClickListener{
            //onClick(R.id.txt_depresion,txt_depresion.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_depresion.text.toString())
        }

        txt_embarazo = binding.txtEmbarazo
        txt_embarazo.setOnClickListener{
            //onClick(R.id.txt_embarazo,txt_embarazo.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_embarazo.text.toString())
        }

        txt_posparto = binding.txtPosparto
        txt_posparto.setOnClickListener{
            //onClick(R.id.txt_posparto,txt_posparto.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_posparto.text.toString())
        }

        txt_otros = binding.txtOtros
        txt_otros.setOnClickListener {
            goToActivity(this,SubForoActivity::class.java,txt_otros.text.toString())
        }

    }

    /**
     * Obtener el rol del usuario
     *
     */
    private fun getDataUser() {
        database.reference.child("Usuarios").child(auth.uid.toString())
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var user: User? = snapshot.getValue(User::class.java)
                    rol = user!!.rol.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    //TO DO("Not yet implemented")
                }
            })
    }
    /**
     *
     * @param activity Activity
     * @param class Class<*>
     * @param foroName String
     *
     */
     fun Context.goToActivity(activity: Activity, classs: Class<*>?, foroName: String) {
        val intent = Intent(activity, classs)
        intent.putExtra("ForoName",foroName)
        startActivity(intent)
        //activity.finish()
    }
}