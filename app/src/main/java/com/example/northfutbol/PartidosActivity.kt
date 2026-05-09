package com.example.northfutbol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Partido

class PartidosActivity : AppCompatActivity() {

    // These would ideally come from a User Session or SharedPreferences
    private val equiposSeguidos = listOf("Real Madrid", "Barcelona")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        cargarPartidosDesdeServidor()
    }

    private fun cargarPartidosDesdeServidor() {
        val layoutSeguidos: LinearLayout = findViewById(R.id.layoutEquiposSeguidos)
        val layoutTodos: LinearLayout = findViewById(R.id.layoutTodosPartidos)

        // Following the logic from MainActivity: READ_ALL operation
        val peticion = PeticionPartido(PeticionPartido.TipoOperacion.READ_ALL, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && respuesta.partidos != null) {
                        val todosLosPartidos = respuesta.partidos

                        // 1. Filter for followed teams
                        val partidosSeguidos = todosLosPartidos.filter { partido ->
                            equiposSeguidos.contains(partido.local.nombre) ||
                                    equiposSeguidos.contains(partido.visitante.nombre)
                        }

                        // 2. Inflate followed matches
                        layoutSeguidos.removeAllViews() // Clear mock data if any
                        partidosSeguidos.forEach { partido ->
                            agregarPartidoAVista(layoutSeguidos, partido)
                        }

                        // 3. Inflate all matches
                        layoutTodos.removeAllViews()
                        todosLosPartidos.forEach { partido ->
                            agregarPartidoAVista(layoutTodos, partido)
                        }

                    } else {
                        Toast.makeText(this@PartidosActivity, "No hay partidos disponibles", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener partidos: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PartidosActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun agregarPartidoAVista(contenedor: LinearLayout, partido: Partido) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_partido, contenedor, false)

        view.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.local.nombre.toString()
        view.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.visitante.nombre.toString()
        view.findViewById<TextView>(R.id.txtFechaPartido).text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(partido.fecha)
        view.findViewById<TextView>(R.id.txtHora).text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(partido.fecha)

        // Cargar escudos de los equipos
        view.findViewById<ImageView>(R.id.imgEquipoLocal).setImageResource(
            EscudosHelper.obtenerEscudo(partido.local.nombre)
        )
        view.findViewById<ImageView>(R.id.imgEquipoVisitante).setImageResource(
            EscudosHelper.obtenerEscudo(partido.visitante.nombre)
        )

        // Set click listener to open match details if needed
        view.setOnClickListener {
            val intent = Intent(this, PartidoActivity::class.java)

            // Pasamos el ID del partido para que la actividad sepa qué cargar
            intent.putExtra("ID_PARTIDO", partido.idPartido)

            startActivity(intent)
        }

        contenedor.addView(view)
    }
}