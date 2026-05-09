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
    private lateinit var txtHoraHeader: TextView
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
        txtHoraHeader = findViewById(R.id.txtMarcador) // ID corregido según tu mensaje
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
            cargarEventosPartido() // Aquí deberías hacer la petición al backend en el futuro
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

                        txtHoraHeader.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(partido.fecha)

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
        val peticion = PeticionEquipo(PeticionEquipo.TipoOperacion.READ, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketEquipo(
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
                            item.findViewById<TextView>(R.id.txtNombre).text = "${jugador.nombre} ${jugador.apellido}"
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

    private fun cargarEventosPartido() {
        val contenedorEventos = findViewById<LinearLayout>(R.id.listaEventos)
        contenedorEventos.removeAllViews()

        for (evento in eventosPartido) {
            val item = layoutInflater.inflate(R.layout.item_evento, contenedorEventos, false)

            val txtMinuto = item.findViewById<TextView>(R.id.tvMinuto)
            val imgTipoEvento = item.findViewById<ImageView>(R.id.ivTipoEvento)
            val txtNombreJugador = item.findViewById<TextView>(R.id.tvNombreJugador)

            // Mostrar minuto
            txtMinuto.text = "${evento.minuto}'"

            // Mostrar nombre del jugador
            txtNombreJugador.text = evento.nombre

            // Asignar icono según tipo de evento
            when (evento.tipoEvento) {
                'G' -> imgTipoEvento.setImageResource(R.drawable.goal)   // Gol
                'M' -> imgTipoEvento.setImageResource(R.drawable.yellow_card) // Amarilla
                'R' -> imgTipoEvento.setImageResource(R.drawable.red_card) // Roja
                'A' -> imgTipoEvento.setImageResource(R.drawable.assist)  // Asistencia
                else -> imgTipoEvento.setImageResource(R.drawable.home)
            }

            contenedorEventos.addView(item)
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

        tabAlineacion.setBackgroundResource(R.drawable.bg_tab_selected)
        tabEventos.setBackgroundResource(android.R.color.transparent)

        tabAlineacion.setTextColor(ContextCompat.getColor(this, R.color.negro))
        tabEventos.setTextColor(ContextCompat.getColor(this, R.color.gris_texto))
    }

    private fun mostrarEventos() {
        scrollAlineacion.visibility = View.GONE
        scrollEventos.visibility = View.VISIBLE

        tabEventos.setBackgroundResource(R.drawable.bg_tab_selected)
        tabAlineacion.setBackgroundResource(android.R.color.transparent)

        tabEventos.setTextColor(ContextCompat.getColor(this, R.color.negro))
        tabAlineacion.setTextColor(ContextCompat.getColor(this, R.color.gris_texto))
    }
}
