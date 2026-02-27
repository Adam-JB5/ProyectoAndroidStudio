package com.example.northfutbol

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

fun Activity.setupBottomBar(bottomBarId: Int) {
    val bottomNav = findViewById<LinearLayout>(bottomBarId)

    // INICIO
    val navInicio = bottomNav.getChildAt(0) as LinearLayout
    val navPartidos = bottomNav.getChildAt(1) as LinearLayout
    val navEquipos = bottomNav.getChildAt(2) as LinearLayout
    val navClasificacion = bottomNav.getChildAt(3) as LinearLayout


    when (this) {
        is MainActivity -> {
            val icon = navInicio.getChildAt(0) as ImageView
            icon.setImageResource(R.drawable.home) // "s" de selected/relleno
        }
        is PartidosActivity -> {
            val icon = navPartidos.getChildAt(0) as ImageView
            icon.setImageResource(R.drawable.field)
        }
        is EquiposActivity -> {
            val icon = navEquipos.getChildAt(0) as ImageView
            icon.setImageResource(R.drawable.team)
        }
        // Nota: Revisa si es EquipoActivity o ClasificacionActivity
        is EquipoActivity -> {
            val icon = navClasificacion.getChildAt(0) as ImageView
            icon.setImageResource(R.drawable.ranking)
        }
    }


    navInicio.setOnClickListener {
        Toast.makeText(this, "Inicio clickeado", Toast.LENGTH_SHORT).show()
        if (this !is MainActivity) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    navPartidos.setOnClickListener {
        Toast.makeText(this, "Partidos clickeado", Toast.LENGTH_SHORT).show()
        if (this !is PartidosActivity) {
            startActivity(Intent(this, PartidosActivity::class.java))
        }
    }

    navEquipos.setOnClickListener {
        Toast.makeText(this, "Equipos clickeado", Toast.LENGTH_SHORT).show()
        if (this !is EquiposActivity) {
            startActivity(Intent(this, EquiposActivity::class.java))
        }
    }

    navClasificacion.setOnClickListener {
        Toast.makeText(this, "Clasificación clickeado", Toast.LENGTH_SHORT).show()
        if (this !is EquipoActivity) {
            startActivity(Intent(this, EquipoActivity::class.java))
        }
    }
}
