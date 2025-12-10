package com.example.piandroidstudio

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.view.View

class EquipoActivity : AppCompatActivity() {

    private lateinit var tabNoticias: TextView
    private lateinit var tabPartidos: TextView
    private lateinit var tabJugadores: TextView
    private lateinit var tabClasificacion: TextView

    private lateinit var contentNoticias: LinearLayout
    private lateinit var contentPartidos: LinearLayout
    private lateinit var contentJugadores: LinearLayout
    private lateinit var contentClasificacion: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.equipo) // ← CAMBIA POR TU XML REAL

        tabNoticias = findViewById(R.id.tabNoticias)
        tabPartidos = findViewById(R.id.tabPartidos)
        tabJugadores = findViewById(R.id.tabJugadores)
        tabClasificacion = findViewById(R.id.tabClasificacion)

        contentNoticias = findViewById(R.id.contentNoticias)
        contentPartidos = findViewById(R.id.contentPartidos)
        contentJugadores = findViewById(R.id.contentJugadores)
        contentClasificacion = findViewById(R.id.contentClasificacion)

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
                R.id.tabClasificacion -> {
                    contentClasificacion.visibility = View.VISIBLE
                    tabClasificacion.setTextColor(Color.BLACK)
                }
            }
        }

        tabNoticias.setOnClickListener(clickListener)
        tabPartidos.setOnClickListener(clickListener)
        tabJugadores.setOnClickListener(clickListener)
        tabClasificacion.setOnClickListener(clickListener)
    }

    private fun hideAll() {
        contentNoticias.visibility = View.GONE
        contentPartidos.visibility = View.GONE
        contentJugadores.visibility = View.GONE
        contentClasificacion.visibility = View.GONE
    }

    private fun resetTabs() {
        tabNoticias.setTextColor(Color.GRAY)
        tabPartidos.setTextColor(Color.GRAY)
        tabJugadores.setTextColor(Color.GRAY)
        tabClasificacion.setTextColor(Color.GRAY)
    }
}
