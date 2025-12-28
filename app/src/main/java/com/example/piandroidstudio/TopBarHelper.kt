package com.example.piandroidstudio

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

fun Activity.setupTopBarOverlay() {

    val btnMenu = findViewById<ImageView>(R.id.btnMenu)
    val btnUsuario = findViewById<ImageView>(R.id.btnUsuario)

    val menuLeft = findViewById<LinearLayout>(R.id.menuMenu)
    val menuRight = findViewById<LinearLayout>(R.id.menuUser)
    val overlay = findViewById<View>(R.id.menuOverlay)
    val scroll = findViewById<ScrollView>(R.id.scrollContent)

    fun closeMenus() {
        if (menuLeft.visibility == View.VISIBLE) {
            menuLeft.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left))
            menuLeft.visibility = View.GONE
        }
        if (menuRight.visibility == View.VISIBLE) {
            menuRight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right))
            menuRight.visibility = View.GONE
        }
        overlay.visibility = View.GONE
        scroll.isEnabled = true
    }

    btnMenu.setOnClickListener {
        closeMenus()
        menuLeft.visibility = View.VISIBLE
        menuLeft.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left))
        overlay.visibility = View.VISIBLE
        scroll.isEnabled = false
    }

    btnUsuario.setOnClickListener {
        closeMenus()
        menuRight.visibility = View.VISIBLE
        menuRight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right))
        overlay.visibility = View.VISIBLE
        scroll.isEnabled = false
    }

    overlay.setOnClickListener { closeMenus() }

    // --- Navegación ejemplo ---
    findViewById<TextView>(R.id.navInicio)?.setOnClickListener {
        startActivity(Intent(this, MainActivity::class.java))
        closeMenus()
    }
}
