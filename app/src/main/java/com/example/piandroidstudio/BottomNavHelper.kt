package com.example.piandroidstudio

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import android.widget.Toast

fun Activity.setupBottomNav(bottomNavId: Int) {
    val bottomNav = findViewById<LinearLayout>(bottomNavId)

    // INICIO
    val navInicio = bottomNav.getChildAt(0) as LinearLayout
    val navPartidos = bottomNav.getChildAt(1) as LinearLayout
    val navEquipos = bottomNav.getChildAt(2) as LinearLayout
    val navClasificacion = bottomNav.getChildAt(3) as LinearLayout

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
