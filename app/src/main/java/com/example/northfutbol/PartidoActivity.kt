package com.example.northfutbol

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PartidoActivity : AppCompatActivity() {

    private var idPartido: Int = -1
    private var idLocal: Int = -1
    private var idVisitante: Int = -1

    private lateinit var tabAlineacion: TextView
    private lateinit var tabEventos: TextView

    private lateinit var scrollAlineacion: ScrollView
    private lateinit var scrollEventos: ScrollView

    // Views
    private lateinit var contenedorJugadores: LinearLayout
    private lateinit var btnEquipo1: Button
    private lateinit var btnEquipo2: Button

    private lateinit var txtNombreLocalHeader: TextView
    private lateinit var txtNombreVisitanteHeader: TextView
    private lateinit var txtMarcador: TextView
    private lateinit var imgEscudoLocal: ImageView
    private lateinit var imgEscudoVisitante: ImageView

    data class Evento(
        val tipoEvento: Char, // 'G', 'M', 'R', 'A'
        val minuto: Int,
        val nombre: String
    )

    // Datos simulados
    private val eventosPartido = listOf(
        Evento( 'G', 5, "Manuel Herrera"),    // Gol de jugador 9 (Manuel Herrera)
        Evento( 'M', 12, "Lucas Romero"),   // Tarjeta amarilla jugador 7 (Lucas Romero)
        Evento( 'G', 23, "Carlos Peña"),  // Gol de jugador 11 (Carlos Peña)
        Evento( 'A', 35, "Diego Fernández"),   // Asistencia de jugador 3 (Diego Fernández)
        Evento( 'R', 44, "Marcos Díaz"),   // Tarjeta roja jugador 4 (Marcos Díaz)
        Evento('G', 67, "Lucas Romero"),   // Gol jugador 7 (Lucas Romero)
        Evento( 'G', 89, "Iván Soto")    // Gol jugador 8 (Iván Soto)
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partido)


        // 1. Obtener ID del Intent
        idPartido = intent.getIntExtra("ID_PARTIDO", -1)

        // TOP / BOTTOM BAR
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        // 2. Inicializar Views
        txtNombreLocalHeader = findViewById(R.id.txtNombreLocalHeader)
        txtNombreVisitanteHeader = findViewById(R.id.txtNombreVisitanteHeader)
        txtMarcador = findViewById(R.id.txtMarcador) // ID corregido según tu mensaje
        imgEscudoLocal = findViewById(R.id.imgEscudoLocal)
        imgEscudoVisitante = findViewById(R.id.imgEscudoVisitante)
        contenedorJugadores = findViewById(R.id.listaJugadores)
        tabAlineacion = findViewById(R.id.tabAlineacion)
        tabEventos = findViewById(R.id.tabEventos)
        btnEquipo1 = findViewById(R.id.btnEquipo1)
        btnEquipo2 = findViewById(R.id.btnEquipo2)
        scrollAlineacion = findViewById(R.id.scrollAlineacion)
        scrollEventos = findViewById(R.id.scrollEventos)

        // 3. Cargar los datos del partido (Esto rellenará idLocal e idVisitante)
        if (idPartido != -1) {
            obtenerDatosPartido()
        } else {
            Toast.makeText(this, "Error: No se recibió el ID del partido", Toast.LENGTH_SHORT).show()
        }

        // 4. Listeners de los botones de equipo (DENTRO DE ALINEACIÓN)
        btnEquipo1.setOnClickListener {
            if (idLocal != -1) {
                cargarJugadoresDesdeServidor(idLocal)
                marcarEquipoActivo(btnEquipo1, btnEquipo2)
            }
        }

        btnEquipo2.setOnClickListener {
            if (idVisitante != -1) {
                cargarJugadoresDesdeServidor(idVisitante)
                marcarEquipoActivo(btnEquipo2, btnEquipo1)
            }
        }

        // 5. Listeners de las Pestañas (Tabs)
        tabAlineacion.setOnClickListener { mostrarAlineacion() }
        tabEventos.setOnClickListener {
            mostrarEventos()
            cargarEventosPartido(null)
        }
    }

    private fun obtenerDatosPartido() {
        val peticion = PeticionPartido(PeticionPartido.TipoOperacion.READ, idPartido)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && respuesta.partido != null) {
                        val partido = respuesta.partido
                        idLocal = partido.local.idEquipo
                        idVisitante = partido.visitante.idEquipo

                        // ACTUALIZAR UI
                        txtNombreLocalHeader.text = partido.local.nombre
                        txtNombreVisitanteHeader.text = partido.visitante.nombre
                        btnEquipo1.text = partido.local.nombre
                        btnEquipo2.text = partido.visitante.nombre

                        // Cargar escudos de los equipos
                        imgEscudoLocal.setImageResource(EscudosHelper.obtenerEscudo(partido.local.nombre))
                        imgEscudoVisitante.setImageResource(EscudosHelper.obtenerEscudo(partido.visitante.nombre))

                        // Habilitar botones ahora que tenemos los IDs
                        btnEquipo1.isEnabled = true
                        btnEquipo2.isEnabled = true

                        //FECHA
                        //txtMarcador.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(partido.fecha)

                        //PUNTUACION
                        txtMarcador.text = partido.golesLocal.toString() + " | " + partido.golesVisitante.toString()

                        val header = findViewById<LinearLayout>(R.id.header)
                        header.background = ZigzagBackground()

                        // Carga inicial de la alineación local
                        cargarJugadoresDesdeServidor(idLocal)
                        marcarEquipoActivo(btnEquipo1, btnEquipo2)
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener partido: ${e.message}")
            }
        }
    }

    private fun cargarJugadoresDesdeServidor(idEquipo: Int) {
        val peticion = PeticionJugador(PeticionJugador.TipoOperacion.READ_BY_TEAM, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketJugador(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    Log.d("DEBUG_APP", "Exito: ${respuesta?.isExito}")
                    Log.d("DEBUG_APP", "Cantidad jugadores: ${respuesta?.jugadores?.size ?: 0}")

                    if (respuesta?.isExito == true && respuesta.jugadores != null) {
                        contenedorJugadores.removeAllViews()
                        for (jugador in respuesta.jugadores) {
                            val item = layoutInflater.inflate(R.layout.item_jugador, contenedorJugadores, false)

                            item.findViewById<TextView>(R.id.txtNumero).text = jugador.dorsal.toString()
                            item.findViewById<TextView>(R.id.txtNombre).text =
                                "${jugador.nombre} ${jugador.apellido ?: ""}".trim()
                            item.findViewById<TextView>(R.id.posicion).text = jugador.posicion

                            contenedorJugadores.addView(item)
                        }
                    } else {
                        Toast.makeText(
                            this@PartidoActivity,
                            "No hay jugadores disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener jugadores: ${e.message}")
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PartidoActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cargarEventosPartido(idEquipoFiltro: Int? = null) {
        val contenedorEventos = findViewById<LinearLayout>(R.id.listaEventos)
        contenedorEventos.removeAllViews()

        val peticion = PeticionEventoPartido(PeticionEventoPartido.TipoOperacion.READ_BY_PARTIDO, idPartido)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketEventoPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && !respuesta.eventoPartidos.isNullOrEmpty()) {

                        // Filtramos por equipo si se pasa un ID, si no mostramos todos
                        val eventosFiltrados = if (idEquipoFiltro != null)
                            respuesta.eventoPartidos.filter { it.jugador.equipo.idEquipo == idEquipoFiltro }
                        else
                            respuesta.eventoPartidos

                        for (evento in eventosFiltrados) {
                            val item = layoutInflater.inflate(R.layout.item_evento, contenedorEventos, false)

                            item.findViewById<TextView>(R.id.tvMinuto).text = "${evento.minuto}'"

                            val esLocal = evento.jugador.equipo.idEquipo == idLocal

                            val icono = when (evento.tipoEvento) {
                                "G" -> R.drawable.goal
                                "M" -> R.drawable.yellow_card
                                "R" -> R.drawable.red_card
                                "A" -> R.drawable.assist
                                else -> R.drawable.home
                            }

                            if (esLocal) {
                                item.findViewById<TextView>(R.id.tvNombreJugadorLocal).apply {
                                    text = "${evento.jugador.nombre} ${evento.jugador.apellido}"
                                    visibility = View.VISIBLE
                                }
                                item.findViewById<ImageView>(R.id.ivTipoEventoLocal).apply {
                                    setImageResource(icono)
                                    visibility = View.VISIBLE
                                }
                            } else {
                                item.findViewById<TextView>(R.id.tvNombreJugadorVisitante).apply {
                                    text = "${evento.jugador.nombre} ${evento.jugador.apellido}"
                                    visibility = View.VISIBLE
                                }
                                item.findViewById<ImageView>(R.id.ivTipoEventoVisitante).apply {
                                    setImageResource(icono)
                                    visibility = View.VISIBLE
                                }
                            }

                            contenedorEventos.addView(item)
                        }

                        if (eventosFiltrados.isEmpty()) {
                            Toast.makeText(this@PartidoActivity, "Sin eventos para este equipo", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this@PartidoActivity, "Sin eventos en este partido", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener eventos: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PartidoActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun marcarEquipoActivo(activo: Button, inactivo: Button) {
        activo.backgroundTintList = null
        inactivo.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.gris_texto)
    }

    private fun mostrarAlineacion() {
        scrollAlineacion.visibility = View.VISIBLE
        scrollEventos.visibility = View.GONE
        findViewById<LinearLayout>(R.id.selectorEquipos).visibility = View.VISIBLE  // ADD

        tabAlineacion.setBackgroundResource(R.drawable.bg_tab_selected)
        tabEventos.setBackgroundResource(android.R.color.transparent)
        tabAlineacion.setTextColor(ContextCompat.getColor(this, R.color.negro))
        tabEventos.setTextColor(ContextCompat.getColor(this, R.color.gris_texto))
    }

    private fun mostrarEventos() {
        scrollAlineacion.visibility = View.GONE
        scrollEventos.visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.selectorEquipos).visibility = View.GONE  // ADD

        tabEventos.setBackgroundResource(R.drawable.bg_tab_selected)
        tabAlineacion.setBackgroundResource(android.R.color.transparent)
        tabEventos.setTextColor(ContextCompat.getColor(this, R.color.negro))
        tabAlineacion.setTextColor(ContextCompat.getColor(this, R.color.gris_texto))
    }
}
