package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.heymama.R
import com.example.heymama.databinding.ActivityMisConsultasBinding
import com.example.heymama.models.Consulta
import java.util.ArrayList

class MisConsultasActivity : AppCompatActivity() {

    private lateinit var recyclerViewConsultas: RecyclerView
    private lateinit var consultasArraylist: ArrayList<Consulta>
    //private lateinit var adapterConsultas:MisConsultasAdapter
    private lateinit var binding: ActivityMisConsultasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisConsultasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerViewConsultas = binding.recyclerViewMisConsultas
        recyclerViewConsultas.layoutManager = LinearLayoutManager(this)
        recyclerViewConsultas.setHasFixedSize(true)

        consultasArraylist = arrayListOf()


    }
}