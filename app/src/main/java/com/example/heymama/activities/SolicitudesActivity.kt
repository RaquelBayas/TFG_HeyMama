package com.example.heymama.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.heymama.databinding.ActivitySolicitudesBinding
import com.example.heymama.fragments.*
import com.example.heymama.interfaces.ItemRecyclerViewListener
import com.google.firebase.auth.FirebaseAuth

class SolicitudesActivity : AppCompatActivity(), ItemRecyclerViewListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var binding: ActivitySolicitudesBinding
    /**
     *
     * @param savedInstanceState Bundle
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid!!

        setUpTabs()
    }

    private fun setUpTabs() {
        val tabsAdapter = ViewPagerAdapter(supportFragmentManager)
        val solicitudesFragment = RequestsFragment()
        val searchFriends = SearchFriendsFragment()
        val bundle = Bundle()
        bundle.putString("uid",uid)
        solicitudesFragment.arguments = bundle
        searchFriends.arguments = bundle
        tabsAdapter.addFragment(solicitudesFragment,"Solicitudes de amistad")
        tabsAdapter.addFragment(searchFriends,"Buscar amigos")
        binding.viewPagerAmigos.adapter = tabsAdapter
        binding.tabs.setupWithViewPager(binding.viewPagerAmigos)
    }
}