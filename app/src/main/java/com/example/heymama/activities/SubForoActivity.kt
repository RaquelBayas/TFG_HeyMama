package com.example.heymama.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.heymama.R
import com.example.heymama.fragments.SubForoFragment

class SubForoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_foro)

        // Home button
        /*var btn_home: Button = findViewById(R.id.btn_home)
        btn_home.setOnClickListener {
            onClick(R.id.btn_home)
        }*/

        // Add question
        var btn_add_question: Button = findViewById(R.id.btn_add_question)
        btn_add_question.setOnClickListener { view ->
            /*val transaccion = supportFragmentManager.beginTransaction()
            val fragmento = SubForoFragment()

            transaccion.replace(R.id.act_subforo,fragmento)
            //transaccion.addToBackStack(null)
            transaccion.commit()*/
            startActivity(Intent(this,PreguntaActivity::class.java))
        }
    }

    fun onClick(view: Int) {
        when(view) {
            R.id.btn_home -> goToActivity(this, HomeActivity::class.java)
            R.id.button2 -> goToActivity(this, ForosActivity::class.java)
            R.id.button3 -> goToActivity(this, PerfilActivity::class.java)
            //R.id.btn_add_question -> goToActivity(this, PreguntaActivity::class.java)
        }
    }

    fun Context.goToActivity(activity: Activity, classs: Class<*>?) {
        val intent = Intent(activity, classs)
        startActivity(intent)
        activity.finish()
    }
}