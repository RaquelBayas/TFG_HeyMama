package com.example.heymama.activities

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.fragments.MoodFragment
import com.example.heymama.interfaces.Utils
import com.example.heymama.models.Mood
import com.example.heymama.models.MoodType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.contains as contains


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, Utils {

    // FirebaseAuth object
    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    private lateinit var textView: TextView
    private lateinit var email: String
    private lateinit var name: String

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    /**
     *
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        firestore = FirebaseFirestore.getInstance()

        val intent = intent
        val rol = intent.getStringExtra("Rol")
        //Toast.makeText(this,rol,Toast.LENGTH_SHORT).show()


        //Instancias para la base de datos y la autenticación
        dataBase = FirebaseDatabase.getInstance("https://heymama-8e2df-default-rtdb.firebaseio.com/")


        //Dentro de la base de datos habrá un nodo "Usuarios" donde se guardan los usuarios de la aplicación
        dataBaseReference = dataBase.getReference("Usuarios")

        // Usuario
        val user: FirebaseUser? = auth.currentUser

        firebaseStore = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com").reference

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_bar);
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_respirar -> {
                    goToActivity(this,RespirarActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_foros -> {goToActivity(this,ForosActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_ajustes -> {
                    goToActivity(this,SettingsActivity::class.java)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

        drawer = findViewById(R.id.drawer_layout)
        var viewNav : View = navigationView.getHeaderView(0)
        toggle = object : ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){

            override fun onDrawerStateChanged(newState: Int) {
                var profileImage_nav = viewNav.findViewById<ImageView>(R.id.nav_header_icon)

                storageReference = firebaseStore.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/perfil")

                GlideApp.with(applicationContext)
                    .load(storageReference)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profileImage_nav)
            }
        }
        drawer.addDrawerListener(toggle)


        /*user?.let {
            for(profile in it.providerData) {
                val providerId = profile.providerId
                val uid = profile.uid
                email = profile.email.toString()
                //name = profile.displayName.toString()

            }
        }*/
        getMoodStatus()
        getUserName(user!!)

        val btn_foros_home : TextView = findViewById(R.id.btn_foros_home)
        btn_foros_home.setOnClickListener{
            onClick(R.id.btn_foros_home)
        }

        val btn_info_home : TextView = findViewById(R.id.btn_info_home)
        btn_info_home.setOnClickListener{
            onClick(R.id.btn_info_home)
        }

        notification()
    }

    /**
     *
     * @param input
     *
     */
    private fun getMoodStatus() {
        var btn_mood_status : Button = findViewById(R.id.btn_mood_status)
        var date = Date().time
        var simpleDateFormat = SimpleDateFormat("dd MM yyyy")
        var dateString = simpleDateFormat.format(date)
        firestore.collection("Mood").document(auth.uid.toString()).collection("Historial").addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            var docs = value!!.documents
            for(doc in docs) {
                if (doc.id == dateString) {
                    btn_mood_status.setOnClickListener {
                        Toast.makeText(this,"Ya has registrado cómo te sientes hoy.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        btn_mood_status.setOnClickListener {
            var moodfragment = MoodFragment()
            moodfragment.show(supportFragmentManager,"moodDialog")
        }
    }

    /**
     *
     * @param user FirebaseUser
     *
     */
    private fun getUserName(user:FirebaseUser) {
        // NOMBRE
        textView = findViewById(R.id.textView)
        firestore.collection("Usuarios").whereEqualTo("email",
            user!!.email).addSnapshotListener { value, error ->
            textView.text = "Bienvenida " + value!!.documents.get(0).get("name").toString()
        }

    }

    /**
     *
     * @param input
     *
     */
    private fun notification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            //val token = task.result
        })
    }

    /**
     *
     * @param item MenuItem
     *
     */
    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.nav_item_perfil  -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_respirar -> onClick(R.id.nav_item_respirar)
            R.id.nav_item_moodregister -> goToActivity(this,MoodActivity::class.java)
            R.id.nav_item_consultas -> goToActivity(this,ContactoActivity::class.java)
            R.id.nav_item_messages -> goToActivity(this,TimelineActivity::class.java)
            R.id.nav_item_solicitudes -> goToActivity(this,SolicitudesActivity::class.java)
            R.id.nav_item_ajustes -> goToActivity(this,SettingsActivity::class.java)
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     *
     * @param savedInstanceState Bundle
     * @param persistentState PersistableBundle
     */
    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        toggle.syncState()
    }

    /**
     *
     * @param newConfig Configuration
     *
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    /**
     *
     * @param item MenuItem
     *
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     *
     * @param view Int
     *
     */
    override fun onClick(view: Int) {
        when(view) {
            R.id.nav_item_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.nav_bottom_item_respirar -> goToActivity(this, RespirarActivity::class.java)
            R.id.nav_bottom_item_ajustes -> goToActivity(this,SettingsActivity::class.java)
            R.id.btn_foros_home -> goToActivity(this, ForosActivity::class.java)
            R.id.btn_info_home -> {
                val intent = Intent(this, InfoActivity::class.java)
                intent.putExtra("Rol","Usuario")
                startActivity(intent)
            }
        }
    }



}
