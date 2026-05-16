package com.example.northfutbol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Partido

class PartidosActivity : AppCompatActivity() {

    private val zigzagBackground = ZigzagBackground()
    private var todosLosPartidos: List<Partido> = emptyList()
    private var filtroActivoId: Int? = null // null = todos

    private lateinit var layoutTodos: LinearLayout
    private lateinit var filtrosLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var scrollContent: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        layoutTodos = findViewById(R.id.layoutTodosPartidos)
        filtrosLayout = findViewById(R.id.filtrosLayout)
        progressBar = findViewById(R.id.progressBar)
        scrollContent = findViewById(R.id.scrollContent)

        cargarPartidosDesdeServidor()
    }

    private fun cargarPartidosDesdeServidor() {
        val layoutSeguidos: LinearLayout = findViewById(R.id.layoutEquiposSeguidos)

        // Mostrar spinner, ocultar contenido
        progressBar.visibility = View.VISIBLE
        scrollContent.visibility = View.GONE

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

                    // Ocultar spinner, mostrar contenido
                    progressBar.visibility = View.GONE
                    scrollContent.visibility = View.VISIBLE

                    // Guardar todos los partidos para filtrar
                    if (respuestaTodos?.isExito == true && !respuestaTodos.partidos.isNullOrEmpty()) {
                        todosLosPartidos = respuestaTodos.partidos
                        mostrarPartidosFiltrados(null)
                        generarBotonesFiltro(respuestaTodos.partidos)
                    } else {
                        Toast.makeText(this@PartidosActivity, "No hay partidos disponibles", Toast.LENGTH_SHORT).show()
                    }

                    // Partidos seguidos (scroll horizontal)
                    if (respuestaSeguidos?.isExito == true && !respuestaSeguidos.partidos.isNullOrEmpty()) {
                        layoutSeguidos.removeAllViews()
                        respuestaSeguidos.partidos.forEach { partido ->
                            agregarPartidoAVista(layoutSeguidos, partido)
                        }
                    } else {
                        Toast.makeText(this@PartidosActivity, "No sigues ningún equipo todavía", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener partidos: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    scrollContent.visibility = View.VISIBLE
                    Toast.makeText(this@PartidosActivity, "Error de conexión", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun generarBotonesFiltro(partidos: List<Partido>) {
        filtrosLayout.removeAllViews()

        // Botón "Todos"
        val btnTodos = crearBotonFiltro("Todos", null)
        marcarBotonActivo(btnTodos)
        filtrosLayout.addView(btnTodos)

        // Equipos únicos de todos los partidos
        val equiposUnicos = (partidos.map { it.local } + partidos.map { it.visitante })
            .distinctBy { it.idEquipo }
            .sortedBy { it.nombre }

        for (equipo in equiposUnicos) {
            val btn = crearBotonFiltro(equipo.nombre, equipo.idEquipo)
            filtrosLayout.addView(btn)
        }
    }

    private fun crearBotonFiltro(texto: String, idEquipo: Int?): Button {
        val btn = Button(this)
        btn.text = texto
        btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        btn.setBackgroundResource(R.color.negro_secciones)
        btn.minWidth = 0
        btn.setPadding(48, 0, 48, 0)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.marginEnd = 16
        btn.layoutParams = params

        btn.setOnClickListener {
            filtroActivoId = idEquipo
            // Resetear todos los botones
            for (i in 0 until filtrosLayout.childCount) {
                val b = filtrosLayout.getChildAt(i) as Button
                b.setBackgroundResource(R.color.negro_secciones)
                b.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
            marcarBotonActivo(btn)
            mostrarPartidosFiltrados(idEquipo)
        }

        return btn
    }

    private fun marcarBotonActivo(btn: Button) {
        btn.setBackgroundResource(R.drawable.bg_tab_selected)
        btn.setTextColor(ContextCompat.getColor(this, R.color.negro))
    }

    private fun mostrarPartidosFiltrados(idEquipo: Int?) {
        layoutTodos.removeAllViews()

        val filtrados = if (idEquipo == null)
            todosLosPartidos
        else
            todosLosPartidos.filter {
                it.local.idEquipo == idEquipo || it.visitante.idEquipo == idEquipo
            }

        if (filtrados.isEmpty()) {
            val tv = TextView(this)
            tv.text = "No hay partidos para este equipo"
            tv.setTextColor(ContextCompat.getColor(this, R.color.gris_texto))
            tv.setPadding(16, 16, 16, 16)
            layoutTodos.addView(tv)
        } else {
            filtrados.forEach { partido ->
                agregarPartidoAVista(layoutTodos, partido)
            }
        }
    }

    private fun agregarPartidoAVista(contenedor: LinearLayout, partido: Partido) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_partido, contenedor, false)

        view.findViewById<TextView>(R.id.txtEquipoLocal).text = partido.local.nombre
        view.findViewById<TextView>(R.id.txtEquipoVisitante).text = partido.visitante.nombre
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

        val cardView = view as androidx.cardview.widget.CardView
        cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        view.findViewById<LinearLayout>(R.id.layoutInternoPartido).background = ZigzagBackground()

        view.setOnClickListener {
            val intent = Intent(this, PartidoActivity::class.java)
            intent.putExtra("ID_PARTIDO", partido.idPartido)
            startActivity(intent)
        }

        contenedor.addView(view)
    }
}