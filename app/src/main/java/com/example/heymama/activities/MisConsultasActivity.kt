package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.adapters.ChatAdapter
import com.example.heymama.models.Consulta
import com.example.heymama.models.Message
import java.util.ArrayList

class MisConsultasActivity : AppCompatActivity() {

    private lateinit var recyclerViewConsultas: RecyclerView
    private lateinit var consultasArraylist: ArrayList<Consulta>
    //private lateinit var adapterConsultas:MisConsultasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_consultas)

        recyclerViewConsultas = findViewById(R.id.recyclerView_consultas)
        recyclerViewConsultas.layoutManager = LinearLayoutManager(this)
        recyclerViewConsultas.setHasFixedSize(true)

        consultasArraylist = arrayListOf()


    }
}