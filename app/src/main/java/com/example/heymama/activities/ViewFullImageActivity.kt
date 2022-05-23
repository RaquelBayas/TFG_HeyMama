package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ActivityViewFullImageBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ViewFullImageActivity : AppCompatActivity() {
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityViewFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance()

        val url = intent.getStringExtra("url").toString()
        GlideApp.with(applicationContext)
            .load(firebaseStore.getReferenceFromUrl(url))
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imageViewer)

    }

}