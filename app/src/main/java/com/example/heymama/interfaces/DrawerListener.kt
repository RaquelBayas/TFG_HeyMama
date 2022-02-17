package com.example.heymama.interfaces

import android.view.View

interface DrawerListener {

    fun onDrawerSlide(drawerView: View, slideOffset: Float) {

    }

    fun onDrawerOpened(drawerView: View) {

    }

    fun onDrawerClosed(drawerView: View) {

    }

    fun onDrawerStateChanged(newState: Int) {

    }
}