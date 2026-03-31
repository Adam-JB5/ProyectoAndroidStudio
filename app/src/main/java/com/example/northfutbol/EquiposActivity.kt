package com.example.northfutbol

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EquiposActivity : AppCompatActivity() {

    private lateinit var tabsGrupo: List<TextView>
    private lateinit var contenedorEquipos: LinearLayout
    private var grupoActivo = 1

    // Datos de prueba por grupo
    private val equiposPorGrupo: Map<Int, List<String>> = mapOf(
        1 to listOf("Real Madrid B", "Atlético B", "Getafe B", "Rayo B", "Leganés B"),
        2 to listOf("Barcelona B", "Girona B", "Espanyol B", "Sabadell", "Badalona"),
        3 to listOf("Valencia B", "Villarreal B", "Levante B", "Hércules", "Castellón"),
        4 to listOf("Sevilla B", "Betis B", "Málaga", "Almería B", "Granada B"),
        5 to listOf("Athletic B", "Real Sociedad B", "Osasuna B", "Alavés B", "Eibar")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipos)
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        contenedorEquipos = findViewById(R.id.contenedorEquipos)

        inicializarSelectorGrupos()
        cargarGrupo(grupoActivo)
        setupClickEquiposEstaticos()
    }

    private fun inicializarSelectorGrupos() {
        val selectorGrupo = findViewById<View>(R.id.selector_grupo)
        tabsGrupo = listOf(
            selectorGrupo.findViewById(R.id.btnGrupo1),
            selectorGrupo.findViewById(R.id.btnGrupo2),
            selectorGrupo.findViewById(R.id.btnGrupo3),
            selectorGrupo.findViewById(R.id.btnGrupo4),
            selectorGrupo.findViewById(R.id.btnGrupo5)
        )

        tabsGrupo.forEachIndexed { index, tab ->
            val grupo = index + 1
            tab.setOnClickListener {
                grupoActivo = grupo
                actualizarTabActivo(grupo)
                cargarGrupo(grupo)
            }
        }

        actualizarTabActivo(1)
    }

    private fun actualizarTabActivo(grupoSeleccionado: Int) {
        tabsGrupo.forEachIndexed { index, tab ->
            if (index + 1 == grupoSeleccionado) {
                tab.setBackgroundResource(R.drawable.bg_tab_selected)
                tab.setTextColor(getColor(R.color.blanco))
            } else {
                tab.setBackgroundColor(Color.TRANSPARENT)
                tab.setTextColor(getColor(R.color.gris_texto))
            }
        }
    }

    private fun cargarGrupo(grupo: Int) {
        contenedorEquipos.removeAllViews()
        val equipos = equiposPorGrupo[grupo] ?: emptyList()
        equipos.forEach { nombre ->
            inflarItem(nombre)
        }
    }

    private fun inflarItem(nombreEquipo: String) {
        val inflater = LayoutInflater.from(this)
        val equipo = inflater.inflate(R.layout.item_equipo, contenedorEquipos, false)

        equipo.findViewById<TextView>(R.id.txt_nombre_equipo).text = nombreEquipo

        contenedorEquipos.addView(equipo)
    }

    private fun setupClickEquiposEstaticos() {
        val contenedorEquipos = findViewById<LinearLayout>(R.id.contenedorEquipos)
        for (i in 0 until contenedorEquipos.childCount) {
            contenedorEquipos.getChildAt(i).setOnClickListener {
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, EquipoActivity::class.java))
            }
        }
    }
}