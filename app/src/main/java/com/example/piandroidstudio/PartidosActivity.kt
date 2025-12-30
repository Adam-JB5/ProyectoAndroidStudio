package com.example.piandroidstudio

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class Partido(
    val equipoLocal: String,
    val equipoVisitante: String,
    val hora: String
)

class PartidosActivity : AppCompatActivity() {

    private val equiposSeguidos = listOf("Real Madrid", "Barcelona") // Ejemplo
    private val todosPartidos = listOf(
        Partido("Real Madrid", "Barcelona", "21:00"),
        Partido("Atl. Madrid", "Sevilla", "19:00"),
        Partido("Valencia", "Real Sociedad", "22:00")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        setupBottomBar(R.id.bottomBar)

        val layoutSeguidos: LinearLayout = findViewById(R.id.layoutEquiposSeguidos)
        val layoutTodos: LinearLayout = findViewById(R.id.layoutTodosPartidos)

        // Filtrar partidos de los equipos seguidos
        val partidosSeguidos = todosPartidos.filter {
            equiposSeguidos.contains(it.equipoLocal) || equiposSeguidos.contains(it.equipoVisitante)
        }

        // Inflar partidos seguidos
        partidosSeguidos.forEach { partido ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_partido, layoutSeguidos, false)
            view.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.equipoLocal
            view.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.equipoVisitante
            view.findViewById<TextView>(R.id.txtHora).text = partido.hora
            layoutSeguidos.addView(view)
        }

        // Inflar todos los partidos
        todosPartidos.forEach { partido ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_partido, layoutTodos, false)
            view.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.equipoLocal
            view.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.equipoVisitante
            view.findViewById<TextView>(R.id.txtHora).text = partido.hora
            layoutTodos.addView(view)
        }
    }
}
