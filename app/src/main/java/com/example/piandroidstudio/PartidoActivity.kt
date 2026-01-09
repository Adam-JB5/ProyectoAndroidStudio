package com.example.piandroidstudio

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PartidoActivity : AppCompatActivity() {

    // Modelo
    data class Jugador(
        val numero: String,
        val nombre: String,
        val posicion: String
    )

    // Datos equipos
    private val equipo1 = listOf(
        Jugador("9", "Manuel Herrera", "DEL"),
        Jugador("7", "Lucas Romero", "MED"),
        Jugador("3", "Diego Fernández", "DEF"),
        Jugador("1", "Pablo Salas", "POR")
    )

    private val equipo2 = listOf(
        Jugador("11", "Carlos Peña", "DEL"),
        Jugador("8", "Iván Soto", "MED"),
        Jugador("4", "Marcos Díaz", "DEF"),
        Jugador("1", "Sergio Luna", "POR")
    )

    // Views
    private lateinit var contenedorJugadores: LinearLayout
    private lateinit var btnEquipo1: Button
    private lateinit var btnEquipo2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partido)

        // TOP / BOTTOM BAR
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        // Referencias
        contenedorJugadores = findViewById(R.id.listaJugadores)
        btnEquipo1 = findViewById(R.id.btnEquipo1)
        btnEquipo2 = findViewById(R.id.btnEquipo2)

        // Equipo inicial
        cargarEquipo(equipo1)
        marcarEquipoActivo(btnEquipo1, btnEquipo2)

        // Listeners
        btnEquipo1.setOnClickListener {
            cargarEquipo(equipo1)
            marcarEquipoActivo(btnEquipo1, btnEquipo2)
        }

        btnEquipo2.setOnClickListener {
            cargarEquipo(equipo2)
            marcarEquipoActivo(btnEquipo2, btnEquipo1)
        }
    }

    private fun cargarEquipo(jugadores: List<Jugador>) {
        for (i in 0 until contenedorJugadores.childCount) {
            val item = contenedorJugadores.getChildAt(i)

            val txtNumero = item.findViewById<TextView>(R.id.txtNumero)
            val txtNombre = item.findViewById<TextView>(R.id.txtNombre)
            val txtPosicion = item.findViewById<TextView>(R.id.posicion)

            val jugador = jugadores[i]

            txtNumero.text = jugador.numero
            txtNombre.text = jugador.nombre
            txtPosicion.text = jugador.posicion
        }
    }

    private fun marcarEquipoActivo(activo: Button, inactivo: Button) {
        activo.backgroundTintList = null
        inactivo.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.gris_texto)
    }
}
