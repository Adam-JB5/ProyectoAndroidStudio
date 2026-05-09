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

        // Igual que en ConfiguracionActivity: leemos el ID de sesión desde SharedPreferences
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val idUsuario = prefs.getInt("idUsuario", 0)

        val peticionTodos = PeticionPartido(PeticionPartido.TipoOperacion.READ_ALL, null)
        val peticionSeguidos = PeticionPartido(PeticionPartido.TipoOperacion.READ_BY_FOLLOWED, idUsuario)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuestaTodos = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticionTodos)

                val respuestaSeguidos = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticionSeguidos)

                withContext(Dispatchers.Main) {

                    // Todos los partidos
                    if (respuestaTodos?.isExito == true && respuestaTodos.partidos != null) {
                        layoutTodos.removeAllViews()
                        respuestaTodos.partidos.forEach { partido ->
                            agregarPartidoAVista(layoutTodos, partido)
                        }
                    } else {
                        Toast.makeText(
                            this@PartidosActivity,
                            "No hay partidos disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // Partidos de equipos seguidos (ya filtrados en el servidor)
                    if (respuestaSeguidos?.isExito == true && respuestaSeguidos.partidos != null) {
                        layoutSeguidos.removeAllViews()
                        respuestaSeguidos.partidos.forEach { partido ->
                            agregarPartidoAVista(layoutSeguidos, partido)
                        }
                    } else {
                        Toast.makeText(
                            this@PartidosActivity,
                            "No sigues ningún equipo todavía",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener partidos: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@PartidosActivity,
                        "Error de conexión",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun agregarPartidoAVista(contenedor: LinearLayout, partido: Partido) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_partido, contenedor, false)

        view.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.local.nombre.toString()
        view.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.visitante.nombre.toString()
        view.findViewById<TextView>(R.id.txtFechaPartido).text =
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(partido.fecha)
        view.findViewById<TextView>(R.id.txtHora).text =
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(partido.fecha)

        view.findViewById<ImageView>(R.id.imgEquipoLocal).setImageResource(
            EscudosHelper.obtenerEscudo(partido.local.nombre)
        )
        view.findViewById<ImageView>(R.id.imgEquipoVisitante).setImageResource(
            EscudosHelper.obtenerEscudo(partido.visitante.nombre)
        )

        view.setOnClickListener {
            val intent = Intent(this, PartidoActivity::class.java)
            intent.putExtra("ID_PARTIDO", partido.idPartido)
            startActivity(intent)
        }

        contenedor.addView(view)
    }
}