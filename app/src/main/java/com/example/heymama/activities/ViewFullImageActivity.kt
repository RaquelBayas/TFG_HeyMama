package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ActivityViewFullImageBinding
import com.google.firebase.storage.FirebaseStorage

class ViewFullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewFullImageBinding
    private lateinit var imageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri = intent.getStringExtra("url").toString()
        GlideApp.with(applicationContext)
            .load(imageUri)
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(true)
            .into(binding.imageViewer)
    }
}