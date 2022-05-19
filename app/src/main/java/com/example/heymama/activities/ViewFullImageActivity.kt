package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ActivityViewFullImageBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ViewFullImageActivity : AppCompatActivity() {
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var binding: ActivityViewFullImageBinding
    private lateinit var imageUri: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance() //CLOUD STORAGE
        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")

        imageUri = intent.getStringExtra("url").toString()
        var path = intent.getStringExtra("path").toString()
        imageUri = firebaseStore.getReference(path).toString()
        storageReference = firebaseStore.getReference("/Usuarios/" + path + "/images/perfil")
        Log.i("imageurl",storageReference.toString())
        GlideApp.with(applicationContext)
            .load(storageReference)
            .error(R.drawable.wallpaper_profile)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(true)
            .into(binding.imageViewer)
    }
}