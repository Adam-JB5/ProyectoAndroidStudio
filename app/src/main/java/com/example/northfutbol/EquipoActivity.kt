package com.example.northfutbol

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Equipo

class EquipoActivity : AppCompatActivity() {

    private var idEquipo: Int = -1

    private lateinit var ivEscudoEquipo: ImageView
    private lateinit var txtNombreEquipo: TextView
    private lateinit var txtCiudadEquipo: TextView
    private lateinit var txtEntrenadorEquipo: TextView
    private lateinit var btnSeguir: CheckBox
    private lateinit var tabNoticias: TextView
    private lateinit var tabPartidos: TextView
    private lateinit var tabJugadores: TextView

    private lateinit var contentNoticias: LinearLayout
    private lateinit var contentPartidos: LinearLayout
    private lateinit var contentJugadores: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipo)

        idEquipo = intent.getIntExtra("ID_EQUIPO", -1)

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        tabNoticias = findViewById(R.id.tabNoticias)
        tabPartidos = findViewById(R.id.tabPartidos)
        tabJugadores = findViewById(R.id.tabJugadores)

        contentNoticias = findViewById(R.id.contentNoticias)
        contentPartidos = findViewById(R.id.contentPartidos)
        contentJugadores = findViewById(R.id.contentJugadores)

        ivEscudoEquipo = findViewById(R.id.ivEscudoEquipo)
        txtNombreEquipo = findViewById(R.id.txtNombreEquipo)
        txtCiudadEquipo = findViewById(R.id.txtCiudadEquipo)
        txtEntrenadorEquipo = findViewById(R.id.txtEntrenadorEquipo)

        btnSeguir = findViewById(R.id.btnSeguir)
        comprobarSeguimiento()

        btnSeguir.setOnCheckedChangeListener(null) // evita disparos al setear estado
        btnSeguir.setOnClickListener {
            if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
        }

        if (idEquipo != -1) {
            obtenerDatosEquipo()
            cargarNoticiasEquipo()
            cargarPartidosEquipo()
            cargarJugadoresEquipo()
        } else {
            Toast.makeText(this, "Error: No se recibió el ID del equipo", Toast.LENGTH_SHORT).show()
        }

        val clickListener = View.OnClickListener { view ->
            hideAll()
            resetTabs()

            when (view.id) {
                R.id.tabNoticias -> {
                    contentNoticias.visibility = View.VISIBLE
                    tabNoticias.setTextColor(Color.WHITE)
                }
                R.id.tabPartidos -> {
                    contentPartidos.visibility = View.VISIBLE
                    tabPartidos.setTextColor(Color.WHITE)
                }
                R.id.tabJugadores -> {
                    contentJugadores.visibility = View.VISIBLE
                    tabJugadores.setTextColor(Color.WHITE)
                }
            }
        }

        tabNoticias.setOnClickListener(clickListener)
        tabPartidos.setOnClickListener(clickListener)
        tabJugadores.setOnClickListener(clickListener)

        // Mostrar la tab inicial
        tabNoticias.setTextColor(Color.WHITE)
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

    private fun obtenerDatosEquipo() {
        val peticion = PeticionEquipo(PeticionEquipo.TipoOperacion.READ, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketEquipo(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && respuesta.equipo != null) {
                        mostrarDatosEquipo(respuesta.equipo)
                    } else {
                        Toast.makeText(this@EquipoActivity, "No se pudo cargar el equipo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_EQUIPO", "Error al obtener equipo: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EquipoActivity, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDatosEquipo(equipo: Equipo) {
        ivEscudoEquipo.setImageResource(EscudosHelper.obtenerEscudo(equipo.nombre))
        txtNombreEquipo.text = equipo.nombre
        txtCiudadEquipo.text = equipo.ciudad ?: "-"
        txtEntrenadorEquipo.text = equipo.entrenador ?: "-"
    }

    private fun idUsuario(): Int =
        getSharedPreferences("usuario", 0).getInt("idUsuario", -1)

    private fun comprobarSeguimiento() {
        val peticion = PeticionUsuarioEquiposSeguidos().apply {
            tipoOperacion = PeticionUsuarioEquiposSeguidos.TipoOperacion.CHECK
            idUsuario = idUsuario()
            idEquipo = this@EquipoActivity.idEquipo
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketUsuarioEquiposSeguidos(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    btnSeguir.setOnCheckedChangeListener(null)
                    btnSeguir.isChecked = respuesta?.isSiguiendo == true
                    btnSeguir.setOnClickListener {
                        if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SEGUIMIENTO", "Error al comprobar seguimiento: ${e.message}")
            }
        }
    }

    private fun seguirEquipo() {
        val peticion = PeticionUsuarioEquiposSeguidos().apply {
            tipoOperacion = PeticionUsuarioEquiposSeguidos.TipoOperacion.CREATE
            idUsuario = idUsuario()
            idEquipo = this@EquipoActivity.idEquipo
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketUsuarioEquiposSeguidos(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true) {
                        Toast.makeText(this@EquipoActivity, "Siguiendo al equipo", Toast.LENGTH_SHORT).show()
                    } else {
                        // Revertir el checkbox si falló
                        btnSeguir.setOnCheckedChangeListener(null)
                        btnSeguir.isChecked = false
                        btnSeguir.setOnClickListener {
                            if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
                        }
                        Toast.makeText(this@EquipoActivity, "Error al seguir el equipo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SEGUIMIENTO", "Error al seguir equipo: ${e.message}")
                withContext(Dispatchers.Main) {
                    btnSeguir.setOnCheckedChangeListener(null)
                    btnSeguir.isChecked = false
                    btnSeguir.setOnClickListener {
                        if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
                    }
                }
            }
        }
    }

    private fun dejarDeSeguirEquipo() {
        val peticion = PeticionUsuarioEquiposSeguidos().apply {
            tipoOperacion = PeticionUsuarioEquiposSeguidos.TipoOperacion.DELETE
            idUsuario = idUsuario()
            idEquipo = this@EquipoActivity.idEquipo
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketUsuarioEquiposSeguidos(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true) {
                        Toast.makeText(this@EquipoActivity, "Has dejado de seguir al equipo", Toast.LENGTH_SHORT).show()
                    } else {
                        // Revertir el checkbox si falló
                        btnSeguir.setOnCheckedChangeListener(null)
                        btnSeguir.isChecked = true
                        btnSeguir.setOnClickListener {
                            if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
                        }
                        Toast.makeText(this@EquipoActivity, "Error al dejar de seguir el equipo", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SEGUIMIENTO", "Error al dejar de seguir: ${e.message}")
                withContext(Dispatchers.Main) {
                    btnSeguir.setOnCheckedChangeListener(null)
                    btnSeguir.isChecked = true
                    btnSeguir.setOnClickListener {
                        if (btnSeguir.isChecked) seguirEquipo() else dejarDeSeguirEquipo()
                    }
                }
            }
        }
    }

    private fun cargarNoticiasEquipo() {
        val peticion = PeticionNoticia(PeticionNoticia.TipoOperacion.READ_BY_TEAM, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketNoticia(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && !respuesta.noticias.isNullOrEmpty()) {
                        contentNoticias.removeAllViews()
                        for (noticia in respuesta.noticias) {
                            val item = layoutInflater.inflate(R.layout.item_noticia, contentNoticias, false)

                            item.findViewById<TextView>(R.id.txtTituloNoticia).text = noticia.titulo
                            item.findViewById<TextView>(R.id.txtSubtituloNoticia).text = noticia.subtitulo
                            Glide.with(this@EquipoActivity)
                                .load(noticia.imagen)
                                .placeholder(R.color.negro_secciones)
                                .centerCrop()
                                .into(item.findViewById(R.id.imgNoticia))

                            item.setOnClickListener {
                                val intent =
                                    Intent(this@EquipoActivity, NoticiaActivity::class.java)
                                intent.putExtra("ID_NOTICIA", noticia.idNoticia)
                                startActivity(intent)
                            }

                            contentNoticias.addView(item)
                        }
                    } else {
                        val tv = TextView(this@EquipoActivity)
                        tv.text = "No hay noticias para este equipo"
                        tv.setTextColor(Color.GRAY)
                        tv.setPadding(16, 16, 16, 16)
                        contentNoticias.addView(tv)
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_EQUIPO", "Error noticias: ${e.message}")
            }
        }
    }

    private fun cargarPartidosEquipo() {
        val peticion = PeticionPartido(PeticionPartido.TipoOperacion.READ_BY_TEAM, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && !respuesta.partidos.isNullOrEmpty()) {
                        contentPartidos.removeAllViews()
                        for (partido in respuesta.partidos) {
                            val item = layoutInflater.inflate(R.layout.item_partido, contentPartidos, false)

                            item.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.local.nombre
                            item.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.visitante.nombre
                            item.findViewById<TextView>(R.id.txtFechaPartido).text =
                                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(partido.fecha)
                            item.findViewById<TextView>(R.id.txtHora).text =
                                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(partido.fecha)
                            item.findViewById<ImageView>(R.id.imgEquipoLocal).setImageResource(
                                EscudosHelper.obtenerEscudo(partido.local.nombre)
                            )
                            item.findViewById<ImageView>(R.id.imgEquipoVisitante).setImageResource(
                                EscudosHelper.obtenerEscudo(partido.visitante.nombre)
                            )

                            val cardView = item as androidx.cardview.widget.CardView
                            cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
                            item.findViewById<LinearLayout>(R.id.layoutInternoPartido).background = ZigzagBackground()

                            item.setOnClickListener {
                                val intent =
                                    Intent(this@EquipoActivity, PartidoActivity::class.java)
                                intent.putExtra("ID_PARTIDO", partido.idPartido)
                                startActivity(intent)
                            }

                            contentPartidos.addView(item)
                        }
                    } else {
                        val tv = TextView(this@EquipoActivity)
                        tv.text = "No hay partidos para este equipo"
                        tv.setTextColor(Color.GRAY)
                        tv.setPadding(16, 16, 16, 16)
                        contentPartidos.addView(tv)
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_EQUIPO", "Error partidos: ${e.message}")
            }
        }
    }

    private fun cargarJugadoresEquipo() {
        val peticion = PeticionJugador(PeticionJugador.TipoOperacion.READ_BY_TEAM, idEquipo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketJugador(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && !respuesta.jugadores.isNullOrEmpty()) {
                        contentJugadores.removeAllViews()
                        for (jugador in respuesta.jugadores) {
                            val item = layoutInflater.inflate(R.layout.item_jugador, contentJugadores, false)

                            item.findViewById<TextView>(R.id.txtNumero).text = jugador.dorsal.toString()
                            item.findViewById<TextView>(R.id.txtNombre).text =
                                "${jugador.nombre} ${jugador.apellido ?: ""}".trim()
                            item.findViewById<TextView>(R.id.posicion).text = jugador.posicion

                            contentJugadores.addView(item)
                        }
                    } else {
                        val tv = TextView(this@EquipoActivity)
                        tv.text = "No hay jugadores para este equipo"
                        tv.setTextColor(Color.GRAY)
                        tv.setPadding(16, 16, 16, 16)
                        contentJugadores.addView(tv)
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_EQUIPO", "Error jugadores: ${e.message}")
            }
        }
    }
}
