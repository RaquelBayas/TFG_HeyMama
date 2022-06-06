package app.example.heymama.activities

import PreferencesManager
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import app.example.heymama.GlideApp
import app.example.heymama.R
import app.example.heymama.Utils
import app.example.heymama.databinding.ActivityHomeBinding
import app.example.heymama.models.Mood
import app.example.heymama.models.MoodType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseReference: DatabaseReference
    private lateinit var firebaseStore: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var textView: TextView
    private lateinit var viewNav: View
    private lateinit var txt_name_nav_header: TextView
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var prefs: PreferencesManager
    private lateinit var binding: ActivityHomeBinding
    /**
     *
     * @constructor
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        prefs = PreferencesManager(this)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dataBase = FirebaseDatabase.getInstance()

        dataBaseReference = dataBase.getReference("Usuarios")
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_bar)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_bottom_item_foros -> {
                    startActivity(Intent(this,ForosActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_bottom_item_ajustes -> {
                    startActivity(Intent(this,SettingsActivity::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }

        drawer = binding.drawerLayout
        viewNav = navigationView.getHeaderView(0)
        toggle = object : ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close){
            override fun onDrawerStateChanged(newState: Int) {
                val profileImage_nav = viewNav.findViewById<ImageView>(R.id.nav_header_icon)
                var photoRef = dataBase.reference.child("Usuarios").child(auth.uid.toString()).child("profilePhoto")
                photoRef.addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value != ""){
                            storageReference = firebaseStore.getReference(snapshot.value.toString())
                            GlideApp.with(applicationContext)
                                .load(storageReference)
                                .error(R.drawable.wallpaper_profile)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(profileImage_nav)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }
        drawer.addDrawerListener(toggle)

        getUserName()
        notification()
        mood()

        binding.btnForosHome.setOnClickListener{
            startActivity(Intent(this,ForosActivity::class.java))
        }
        binding.btnInfoHome.setOnClickListener{
            val intent = Intent(this, InfoActivity::class.java)
            intent.putExtra("Rol","Usuario")
            startActivity(intent)
        }
    }

    /**
     * Este método controla el registro del estado de ánimo
     */
    private fun mood() {
        val btn_mood_feliz = binding.btnMoodHomeFeliz
        val btn_mood_bien = binding.btnMoodHomeBien
        val btn_mood_regular = binding.btnMoodHomeRegular
        val btn_mood_mal = binding.btnMoodHomeMal
        val btn_mood_triste = binding.btnMoodHomTriste
        val date = Date().time
        val simpleDateFormat = SimpleDateFormat("dd MM yyyy")
        val dateString = simpleDateFormat.format(date)

        val listMoods = arrayListOf(btn_mood_feliz,btn_mood_bien,btn_mood_regular,btn_mood_mal,btn_mood_triste)
        val listMoodsTypes = arrayListOf(*MoodType.values())
        for((index,button) in listMoods.withIndex()) {
            button.setOnClickListener {
                if (!Utils.isNetworkAvailable(this)) {
                    Utils.alertDialogInternet(this)
                } else if (Utils.isNetworkAvailable(this)) {
                    val mood = Mood(listMoodsTypes[index].ordinal.toString(),
                        listMoodsTypes[index].name, Date())
                    firestore.collection("Mood").document(auth.uid.toString())
                        .collection("Historial").document(dateString).set(mood)
                    Toast.makeText(this,
                        "Has registrado tu estado de ánimo correctamente.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        firestore.collection("Mood").document(auth.uid.toString()).collection("Historial").addSnapshotListener { value, error ->
            if(error != null) {
                return@addSnapshotListener
            }
            val docs = value!!.documents
            for(doc in docs) {
                if (doc.id == dateString) {
                    for(button in listMoods){
                        button.isClickable = false
                    }
                }
            }
        }
    }

    /**
     * Obtiene el nombre del usuario y lo añade en el mensaje de bienvenida y el header del navigation lateral.
     */
    private fun getUserName() {
        textView = findViewById(R.id.textView)
        firestore.collection("Usuarios").document(auth.uid.toString()).addSnapshotListener { value, error ->
            if(value!!.exists()) {
                textView.text = "Bienvenida " + value["name"].toString()
                txt_name_nav_header = viewNav.findViewById(R.id.txt_name_nav_header)
                txt_name_nav_header.text = value["name"].toString()
            }
        }

    }


    private fun notification() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
               return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
        })
    }

    /**
     * Menú lateral
     * @param item MenuItem
     *
     */
    override fun onNavigationItemSelected(item: MenuItem) : Boolean {
        when (item.itemId) {
            R.id.nav_item_perfil  -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_item_notifications -> startActivity(Intent(this,NotificationsActivity::class.java))
            R.id.nav_item_respirar -> startActivity(Intent(this,RespirarActivity::class.java))
            R.id.nav_item_moodregister -> startActivity(Intent(this,MoodActivity::class.java))
            R.id.nav_item_consultas -> startActivity(Intent(this,ContactoActivity::class.java))
            R.id.nav_item_messages -> startActivity(Intent(this,TimelineActivity::class.java))
            R.id.nav_item_solicitudes -> {
                if (!Utils.isNetworkAvailable(this)) {
                    Utils.alertDialogInternet(this)
                } else if (Utils.isNetworkAvailable(this)) {
                startActivity(Intent(this,SolicitudesActivity::class.java))}}
            R.id.nav_item_ajustes -> startActivity(Intent(this,SettingsActivity::class.java))
            R.id.nav_item_logout -> logOut()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Cierra sesión del usuario.
     */
    private fun logOut() {
        prefs.editor?.clear()
        prefs.editor?.commit()
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    /**
     * Cambia el estado del usuario a "offline".
     */
    override fun onPause() {
        super.onPause()
        app.example.heymama.Utils.updateStatus("offline")
    }

    /**
     * Cambia el estado del usuario a "online".
     */
    override fun onResume() {
        super.onResume()
        app.example.heymama.Utils.updateStatus("online")
    }

}
