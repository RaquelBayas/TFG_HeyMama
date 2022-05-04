package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.example.heymama.*
import com.example.heymama.databinding.ActivityContactoBinding
import com.example.heymama.models.Consulta
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ContactoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var spinnerConsultas: Spinner
    private lateinit var temas: Array<String>

    private lateinit var btn_send_consulta: Button
    private lateinit var btn_mis_consultas: Button
    private lateinit var binding: ActivityContactoBinding

    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE

        binding.bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_home -> finish()
                R.id.nav_bottom_item_foros -> {
                    finish()
                    startActivity(Intent(this,ForosActivity::class.java))
                }
                R.id.nav_bottom_item_ajustes -> {
                    finish()
                    startActivity(Intent(this,SettingsActivity::class.java))
                }
            }
        }

        spinnerConsultas = findViewById(R.id.spinnerConsultas)
        temas = resources.getStringArray(R.array.temasConsultas)
        val adapter = ArrayAdapter(this,R.layout.spinner_item,temas)
        spinnerConsultas.adapter = adapter

        binding.btnSendConsulta.setOnClickListener {
            sendConsulta()
        }

        binding.btnMisConsultas.setOnClickListener {
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
        val spinnerConsultas : Spinner = binding.spinnerConsultas
        var txt_consulta : EditText = binding.editTextConsulta
        var txt_tema : String = spinnerConsultas.selectedItem.toString()
        var user : String = auth.uid.toString()
        var ref = firestore.collection("Consultas").document(txt_tema).collection("Consultas").document()

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