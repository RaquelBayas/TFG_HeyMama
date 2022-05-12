package com.example.heymama.activities

import android.os.Bundle
import android.view.FrameMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.heymama.R
import com.example.heymama.databinding.ActivityInfoAppBinding
import com.example.heymama.fragments.VersionAppFragment


class InfoAppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openFragments()
    }

    private fun openFragments(){
        binding.txtTutorial.setOnClickListener {

        }

        binding.txtTerminos.setOnClickListener {

        }

        binding.txtPoliticas.setOnClickListener {

        }

        binding.txtVersion.setOnClickListener {
            val fragment = VersionAppFragment()
            supportFragmentManager.beginTransaction().replace(R.id.activityInfoApp,fragment).addToBackStack(null).commit()
        }
    }

}