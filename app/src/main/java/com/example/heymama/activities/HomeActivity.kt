package com.example.heymama.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.interfaces.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, Utils{

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    lateinit var firebaseStore: FirebaseStorage
    lateinit var firestore: FirebaseFirestore
    lateinit var storageReference: StorageReference

    private lateinit var textView: TextView
    private lateinit var email: String
    private lateinit var name: String

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intent = intent
        val rol = intent.getStringExtra("Rol")
        Toast.makeText(this,rol,Toast.LENGTH_SHORT).show()


        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")
        auth = FirebaseAuth.getInstance()

        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        val user: FirebaseUser? = auth.currentUser
        // ID en la BBDD
        val userDB: DatabaseReference = dataBaseReference.child(user!!.uid)

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> goToActivity(this,RespirarActivity::class.java)
                R.id.nav_bottom_item_foros -> goToActivity(this,ForosActivity::class.java)

            }
        }

        loadPicture(navigationView)

        user?.let {
            for(profile in it.providerData) {
                val providerId = profile.providerId
                val uid = profile.uid
                email = profile.email.toString()
                //name = profile.displayName.toString()

            }
        }

        // NOMBRE
        textView = findViewById(R.id.textView)
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Usuarios").whereEqualTo("Email",user.email).addSnapshotListener { value, error ->
            textView.text = "Bienvenida " + value!!.documents.get(0).get("Name").toString()
        }


        var txt_foros : TextView = findViewById(R.id.txt_foros)
        txt_foros.setOnClickListener{
            onClick(R.id.txt_foros)
        }

        var txt_informacion : TextView = findViewById(R.id.txt_informacion)
        txt_informacion.setOnClickListener{
            onClick(R.id.txt_informacion)
        }

        drawer.addDrawerListener(toggle)


    }


    fun loadPicture(navigationView: NavigationView) {
        // Comprueba si existe imagen de perfil en la bbdd
        var viewNav : View = navigationView.getHeaderView(0)
        var profileImage_nav = viewNav.findViewById<ImageView>(R.id.nav_header_icon)

        storageReference = firebaseStore.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/perfil")


        GlideApp.with(applicationContext)
            .load(storageReference)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(profileImage_nav)

        Toast.makeText(this, "Picture updated", Toast.LENGTH_SHORT).show()

    }


    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.nav_item_perfil  -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }// Toast.makeText(this, "Item 1", Toast.LENGTH_SHORT).show()
            R.id.nav_item_respirar -> onClick(R.id.nav_item_respirar)
            R.id.nav_bottom_item_respirar -> onClick(R.id.nav_bottom_item_respirar)
            R.id.nav_item_consultas -> goToActivity(this,ContactoActivity::class.java)

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onClick(view: Int) {
        when(view) {
            R.id.nav_item_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.nav_bottom_item_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.txt_foros -> goToActivity(this, ForosActivity::class.java)
            R.id.txt_informacion -> {
                val intent = Intent(this, InfoActivity::class.java)
                intent.putExtra("Rol","Usuario")
                startActivity(intent)
                //goToActivity(this, InfoActivity::class.java)
            }
        }
    }


}
/*
private fun View.setOnClickListener(btnIconProfile: Int) {

}*/
