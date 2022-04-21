package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.heymama.R
import com.google.firebase.firestore.FirebaseFirestore

class RespuestaConsultaActivity : AppCompatActivity() {

    private lateinit var id_consulta : String
    private lateinit var tema_consulta : String
    private lateinit var id_user_consulta : String
    private lateinit var consulta : String

    private lateinit var txt_consulta_post_respuesta: TextView
    private lateinit var txt_consulta_user_respuesta: TextView

    private lateinit var firestore: FirebaseFirestore


    /**
     *
     * @param savedInstanceState Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respuesta_consulta)

        firestore = FirebaseFirestore.getInstance()

        val intent = intent
        if (intent.hasExtra("id_consulta") && intent.hasExtra("tema_consulta") && intent.hasExtra("id_user_consulta") && intent.hasExtra("consulta")) {
            id_consulta = intent.getStringExtra("id_consulta").toString()
            tema_consulta = intent.getStringExtra("tema_consulta").toString()
            id_user_consulta = intent.getStringExtra("id_user_consulta").toString()
            consulta = intent.getStringExtra("consulta").toString()
        }

        putDataConsulta(id_consulta,tema_consulta,id_user_consulta,consulta)
        getDataConsulta(id_consulta)

    }

    /**
     *
     * @param id_consulta String
     *
     */
    private fun getDataConsulta(id_consulta: String) {

    }

    /**
     *
     * @param id_consulta String
     * @param tema_consulta String
     * @param id_user_consulta String
     *
     */
    private fun putDataConsulta(id_consulta: String,tema_consulta: String, id_user_consulta: String, consulta: String) {
        txt_consulta_post_respuesta = findViewById(R.id.txt_consulta_post_respuesta)
        txt_consulta_post_respuesta.text = consulta

        txt_consulta_user_respuesta = findViewById(R.id.txt_consulta_user_respuesta)
        txt_consulta_user_respuesta.text = id_user_consulta

    }
}