package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.heymama.*
import com.example.heymama.models.Consulta
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ContactoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var spinnerConsultas: Spinner
    private lateinit var temas: Array<String>

    private lateinit var btn_send_consulta: Button
    private lateinit var btn_mis_consultas: Button

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacto)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_home -> finish()
                R.id.nav_item_respirar -> goToActivity(this,RespirarActivity::class.java)
            }
        }

        spinnerConsultas = findViewById(R.id.spinnerConsultas)
        temas = resources.getStringArray(R.array.temasConsultas)
        val adapter = ArrayAdapter(this,R.layout.spinner_item,temas)
        spinnerConsultas.adapter = adapter

        btn_send_consulta = findViewById(R.id.btn_send_consulta)
        btn_send_consulta.setOnClickListener {
            sendConsulta()
        }

        btn_mis_consultas = findViewById(R.id.btn_mis_consultas)
        btn_mis_consultas.setOnClickListener {
            misConsultas()
        }

    }

    private fun misConsultas() {
        val intent = Intent(this, ConsultasActivity::class.java)
        startActivity(intent)
    }

    /**
     *
     * @param input
     *
     */
    private fun sendConsulta() {
        val spinnerConsultas : Spinner = findViewById(R.id.spinnerConsultas)
        var txt_consulta : EditText = findViewById(R.id.editText_consulta)
        var txt_tema : String = spinnerConsultas.selectedItem.toString()
        var user : String = auth.uid.toString()
        var ref = firestore.collection("Consultas").document(txt_tema).collection("Consultas").document()


        //.collection(auth.uid.toString()).document()
        /*var refUser = firestore.collection("Usuarios").document(auth.uid.toString()).get()
        refUser.addOnSuccessListener { document ->
            if (document != null) {
                user = document.toObject(User::class.java)
            }
        }*/


        var consulta = Consulta(ref.id,user,txt_tema,txt_consulta.text.toString(),Date())

        if(txt_consulta.text.isNotEmpty()) {
            ref.set(consulta)
            Toast.makeText(this,"Consulta enviada correctamente",Toast.LENGTH_SHORT).show()
            txt_consulta.setText("")
        }

    }

    /**
     *
     * @param activity Activity
     * @param classs Class<*>
     */
    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
    }
}