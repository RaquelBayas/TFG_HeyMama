package com.example.heymama.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.heymama.R
import com.example.heymama.databinding.ActivityInfoAppBinding
import com.example.heymama.fragments.PoliticasFragment
import com.example.heymama.fragments.VersionAppFragment


class InfoAppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openFragments()
    }

    /**
     * Este método permite comprobar el tutorial de la app o la versión.
     */
    private fun openFragments(){
        binding.txtPoliticas.setOnClickListener {
            val fragment = PoliticasFragment()
            supportFragmentManager.beginTransaction().replace(R.id.activityInfoApp,fragment).addToBackStack(null).commit()
        }

        binding.txtVersion.setOnClickListener {
            val fragment = VersionAppFragment()
            supportFragmentManager.beginTransaction().replace(R.id.activityInfoApp,fragment).addToBackStack(null).commit()
        }
    }

}