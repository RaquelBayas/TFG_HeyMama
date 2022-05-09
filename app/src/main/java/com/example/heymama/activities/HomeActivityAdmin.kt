package com.example.heymama.activities

import PreferencesManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heymama.GlideApp
import com.example.heymama.R
import com.example.heymama.Utils
import com.example.heymama.databinding.ActivityHomeAdminBinding
import com.example.heymama.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeActivityAdmin : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeAdminBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var prefs: PreferencesManager
    private lateinit var viewNav: View
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)
        binding = ActivityHomeAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.txtListaUsuarios.setOnClickListener{
            startActivity(Intent(this,ListaUsuariosActivity::class.java))
        }

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout_home_admin)
        viewNav = navigationView.getHeaderView(0)
        toggle = object : ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){

            override fun onDrawerStateChanged(newState: Int) {
                var profileImage_nav = viewNav.findViewById<ImageView>(R.id.nav_header_icon)

            }
        }
        drawer.addDrawerListener(toggle)

        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_foros ->
                    startActivity(Intent(this,ForosActivity::class.java))

                R.id.nav_bottom_item_ajustes ->
                    startActivity(Intent(this,SettingsActivity::class.java))
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    private fun logOut() {
        prefs.editor?.clear()
        prefs.editor?.commit()

        val intent = Intent(this, Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.nav_item_logout -> logOut()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

}