package com.example.heymama.activities

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.heymama.R
import com.example.heymama.fragments.SolicitudesFragment
import com.example.heymama.interfaces.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class HomeActivityProf : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener,
    Utils {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_prof)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout_home_prof)
        toggle = ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //BARRA DE NAVEGACIÓN INFERIOR
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemReselectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_foros -> goToActivity(this,ForosActivity::class.java)
                R.id.nav_bottom_item_respirar -> goToActivity(this, RespirarActivity::class.java)
                R.id.nav_bottom_item_ajustes -> goToActivity(this,SettingsActivity::class.java)
            }
        }

        var txt_consultas : TextView = findViewById(R.id.txt_consultas)
        txt_consultas.setOnClickListener{
            onClick(R.id.txt_consultas)
        }

        var txt_foros : TextView = findViewById(R.id.txt_foros)
        txt_foros.setOnClickListener{
            onClick(R.id.txt_foros)
        }

        var txt_informacion : TextView = findViewById(R.id.txt_informacion)
        txt_informacion.setOnClickListener{
            onClick(R.id.txt_informacion)
        }
    }

    // MENÚ LATERAL
    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.nav_bottom_item_home  -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_respirar -> onClick(R.id.nav_item_respirar)
            R.id.nav_item_consultas -> goToActivity(this,ContactoActivity::class.java)
            R.id.nav_item_messages -> goToActivity(this,TimelineActivity::class.java)
            R.id.nav_item_ajustes -> goToActivity(this,SettingsActivity::class.java)
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun goToFragment() {
        val fragment = SolicitudesFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.home_prof_layout,fragment)
        fragmentTransaction.commit()
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
            R.id.txt_consultas -> goToActivity(this,ConsultasActivity::class.java)
            R.id.txt_foros -> goToActivity(this, ForosActivity::class.java)
            R.id.txt_informacion -> {
                val intent = Intent(this, InfoActivity::class.java)
                intent.putExtra("Rol","Profesional")
                startActivity(intent)
                //goToActivity(this, InfoActivity::class.java)
            }
        }
    }

}