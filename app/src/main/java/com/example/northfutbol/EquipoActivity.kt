package com.example.northfutbol

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EquipoActivity : AppCompatActivity() {

    private lateinit var tabNoticias: TextView
    private lateinit var tabPartidos: TextView
    private lateinit var tabJugadores: TextView
    private lateinit var tabClasificacion: TextView

    private lateinit var contentNoticias: LinearLayout
    private lateinit var contentPartidos: LinearLayout
    private lateinit var contentJugadores: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipo) // ← CAMBIA POR TU XML REAL

        setupTopBarOverlay()

        setupBottomBar(R.id.bottomBar)

        tabNoticias = findViewById(R.id.tabNoticias)
        tabPartidos = findViewById(R.id.tabPartidos)
        tabJugadores = findViewById(R.id.tabJugadores)

        contentNoticias = findViewById(R.id.contentNoticias)
        contentPartidos = findViewById(R.id.contentPartidos)
        contentJugadores = findViewById(R.id.contentJugadores)

        val clickListener = View.OnClickListener { view ->
            hideAll()
            resetTabs()

            when (view.id) {
                R.id.tabNoticias -> {
                    contentNoticias.visibility = View.VISIBLE
                    tabNoticias.setTextColor(Color.BLACK)
                }
                R.id.tabPartidos -> {
                    contentPartidos.visibility = View.VISIBLE
                    tabPartidos.setTextColor(Color.BLACK)
                }
                R.id.tabJugadores -> {
                    contentJugadores.visibility = View.VISIBLE
                    tabJugadores.setTextColor(Color.BLACK)
                }
            }
        }

        tabNoticias.setOnClickListener(clickListener)
        tabPartidos.setOnClickListener(clickListener)
        tabJugadores.setOnClickListener(clickListener)
    }

    private fun hideAll() {
        contentNoticias.visibility = View.GONE
        contentPartidos.visibility = View.GONE
        contentJugadores.visibility = View.GONE
    }

    private fun resetTabs() {
        tabNoticias.setTextColor(Color.GRAY)
        tabPartidos.setTextColor(Color.GRAY)
        tabJugadores.setTextColor(Color.GRAY)
    }
}
