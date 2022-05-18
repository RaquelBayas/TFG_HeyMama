package com.example.heymama.activities

import PreferencesManager
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.databinding.ActivityHomeProfBinding
import com.example.heymama.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomeActivityProf : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener{

    private lateinit var drawer: DrawerLayout
    private lateinit var viewNav: View
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityHomeProfBinding
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var prefs: PreferencesManager
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var txt_name_nav_header: TextView
    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        binding = ActivityHomeProfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance("gs://heymama-8e2df.appspot.com")
        storageReference = firebaseStorage.reference

        toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_bar)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawer = binding.drawerLayoutHomeProf
        viewNav = navigationView.getHeaderView(0)
        toggle = object : ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){
            override fun onDrawerStateChanged(newState: Int) {
                var profileImageNav = viewNav.findViewById<ImageView>(R.id.nav_header_icon)
                storageReference = firebaseStorage.getReference("/Usuarios/"+auth.currentUser?.uid+"/images/perfil")
                GlideApp.with(applicationContext)
                    .load(storageReference)
                    .error(R.drawable.wallpaper_profile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profileImageNav)
            }
        }
        drawer.addDrawerListener(toggle)

        getUserName()
        initBottomNav()
        initButtons()
    }


    private fun initBottomNav() {
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_foros -> startActivity(Intent(this,ForosActivity::class.java))
                R.id.nav_bottom_item_ajustes ->  startActivity(Intent(this,SettingsActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener false
        }
    }
    private fun initButtons() {
        binding.txtConsultas.setOnClickListener{
            startActivity(Intent(this,ConsultasActivity::class.java))
        }

        binding.txtForos.setOnClickListener{
            startActivity(Intent(this,ForosActivity::class.java))
        }

        binding.txtInformacion.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("Rol","Profesional")
            startActivity(intent)
        }
    }

    /**
     * Menú lateral
     * @param item MenuItem
     *
     */
    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.nav_bottom_item_home  -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_consultas -> startActivity(Intent(this,ContactoActivity::class.java))
            R.id.nav_item_timeline -> startActivity(Intent(this,TimelineActivity::class.java))
            R.id.nav_item_ajustes -> startActivity(Intent(this,SettingsActivity::class.java))
            R.id.nav_item_logout -> logOut()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logOut() {
        prefs.editor?.clear()
        prefs.editor?.commit()

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun getUserName() {
        database.reference.child("Usuarios").child(auth.uid.toString()).child("name").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                txt_name_nav_header = viewNav.findViewById(R.id.txt_name_nav_header)
                txt_name_nav_header.text = snapshot.value.toString()

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
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
}