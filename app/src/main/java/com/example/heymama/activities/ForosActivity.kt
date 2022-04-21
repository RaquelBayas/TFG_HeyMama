package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.heymama.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.example.heymama.interfaces.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.storage.FirebaseStorage

class ForosActivity : AppCompatActivity(), Utils{
    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var firebaseStore: FirebaseStorage

    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foros)

        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        // Usuario
        val user: FirebaseUser? = auth.currentUser

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")


        var txt_depresion : TextView = findViewById(R.id.txt_depresion)
        txt_depresion.setOnClickListener{
            //onClick(R.id.txt_depresion,txt_depresion.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_depresion.text.toString())
        }

        var txt_embarazo : TextView = findViewById(R.id.txt_embarazo)
        txt_embarazo.setOnClickListener{
            //onClick(R.id.txt_embarazo,txt_embarazo.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_embarazo.text.toString())
        }

        var txt_posparto : TextView = findViewById(R.id.txt_posparto)
        txt_posparto.setOnClickListener{
            //onClick(R.id.txt_posparto,txt_posparto.text.toString())
            goToActivity(this,SubForoActivity::class.java,txt_posparto.text.toString())
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> {
                    goToActivity(this,RespirarActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_home -> {
                    finish()
                    goToActivity(this,HomeActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_ajustes -> {
                    goToActivity(this,SettingsActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
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