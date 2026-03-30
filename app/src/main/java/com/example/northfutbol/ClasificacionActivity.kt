package com.example.northfutbol

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClasificacionActivity : AppCompatActivity() {

    private lateinit var tabsGrupo: List<TextView>
    private lateinit var contenedorClasificacion: LinearLayout
    private var grupoActivo = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clasificacion)


        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        contenedorClasificacion = findViewById(R.id.contenedorClasificacion)

        inicializarSelectorGrupos()

        // Cargamos el grupo 1 por defecto al abrir la pantalla
        cargarClasificacion("1")
    }

    private fun inicializarSelectorGrupos() {
        tabsGrupo = listOf(
            findViewById(R.id.btnGrupo1),
            findViewById(R.id.btnGrupo2),
            findViewById(R.id.btnGrupo3),
            findViewById(R.id.btnGrupo4),
            findViewById(R.id.btnGrupo5)
        )

        tabsGrupo.forEachIndexed { index, tab ->
            val grupo = index + 1
            tab.setOnClickListener {
                grupoActivo = grupo
                actualizarTabActivo(grupo)
                cargarClasificacion(grupo.toString())
            }
        }

        // Marcamos el grupo 1 como activo por defecto
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

    private fun cargarClasificacion(grupo: String) {
        contenedorClasificacion.removeAllViews()

        // DATOS DE PRUEBA
        val equiposPrueba = listOf(
            EquipoClasificacion("Real Madrid CF", 20, 15, 3, 2, 45, 15, 30, 48),
            EquipoClasificacion("FC Barcelona", 20, 14, 4, 2, 40, 18, 22, 46),
            EquipoClasificacion("Atlético de Madrid", 20, 13, 3, 4, 35, 20, 15, 42),
            EquipoClasificacion("Sevilla FC", 20, 11, 4, 5, 30, 22, 8, 37),
            EquipoClasificacion("Valencia CF", 20, 10, 5, 5, 28, 25, 3, 35),
            EquipoClasificacion("Villarreal CF", 20, 9, 4, 7, 27, 26, 1, 31),
            EquipoClasificacion("Athletic Club", 20, 8, 5, 7, 25, 27, -2, 29),
            EquipoClasificacion("Real Sociedad", 20, 7, 6, 7, 24, 28, -4, 27),
            EquipoClasificacion("Betis", 20, 6, 5, 9, 20, 30, -10, 23),
            EquipoClasificacion("Getafe CF", 20, 5, 4, 11, 18, 33, -15, 19),
            EquipoClasificacion("Celta de Vigo", 20, 4, 4, 12, 16, 35, -19, 16),
            EquipoClasificacion("Espanyol", 20, 3, 3, 14, 14, 40, -26, 12),
            EquipoClasificacion("Almería", 20, 2, 2, 16, 10, 45, -35, 8)
        )

        equiposPrueba.forEachIndexed { index, equipo ->
            inflarFila(equipo, index + 1, equiposPrueba.size)
        }
    }

//    private fun cargarClasificacion(grupo: String) {
//        contenedorClasificacion.removeAllViews()
//
//        val peticion = PeticionClasificacion(
//            PeticionClasificacion.TipoOperacion.READ_BY_GRUPO,
//            grupo
//        )
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val respuesta = ClienteSocketClasificacion(
//                    ClienteConfig.getServerIP(),
//                    ClienteConfig.PUERTO_SERVIDOR
//                ).enviarPeticion(peticion)
//
//                withContext(Dispatchers.Main) {
//                    Log.d("DEBUG_APP", "Exito: ${respuesta?.isExito}")
//                    Log.d("DEBUG_APP", "Equipos en clasificación: ${respuesta?.equipos?.size ?: 0}")
//
//                    if (respuesta?.isExito == true && respuesta.equipos != null) {
//                        respuesta.equipos.forEachIndexed { index, equipo ->
//                            inflarFila(equipo, index + 1, respuesta.equipos.size)
//                        }
//                    } else {
//                        Toast.makeText(
//                            this@ClasificacionActivity,
//                            "No hay datos para este grupo",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("ERROR_SERVER", "Error al cargar clasificación: ${e.message}")
//                e.printStackTrace()
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@ClasificacionActivity,
//                        "Error: ${e.localizedMessage}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//        }
//    }

    private fun inflarFila(equipo: EquipoClasificacion, posicion: Int, totalEquipos: Int) {
        val inflater = LayoutInflater.from(this)
        val fila = inflater.inflate(R.layout.item_fila_clasificacion, contenedorClasificacion, false)

        fila.findViewById<TextView>(R.id.txtPosicion).text = posicion.toString()
        fila.findViewById<TextView>(R.id.txtNombreEquipo).text = equipo.nombre
        fila.findViewById<TextView>(R.id.txtPJ).text = equipo.pj.toString()
        fila.findViewById<TextView>(R.id.txtPG).text = equipo.pg.toString()
        fila.findViewById<TextView>(R.id.txtPE).text = equipo.pe.toString()
        fila.findViewById<TextView>(R.id.txtPP).text = equipo.pp.toString()
        fila.findViewById<TextView>(R.id.txtGF).text = equipo.gf.toString()
        fila.findViewById<TextView>(R.id.txtGC).text = equipo.gc.toString()
        fila.findViewById<TextView>(R.id.txtGD).text = equipo.gd.toString()
        fila.findViewById<TextView>(R.id.txtPuntos).text = equipo.puntos.toString()

        // Indicador lateral de color según posición (reglas Segunda RFEF)
        val indicador = fila.findViewById<View>(R.id.indicadorPosicion)
        when {
            posicion <= 2                      -> indicador.setBackgroundColor(Color.parseColor("#2196F3")) // Ascenso directo
            posicion <= 4                      -> indicador.setBackgroundColor(Color.parseColor("#4CAF50")) // Promoción
            posicion >= totalEquipos - 2       -> indicador.setBackgroundColor(Color.parseColor("#F44336")) // Descenso
            else                               -> indicador.setBackgroundColor(Color.TRANSPARENT)
        }

        contenedorClasificacion.addView(fila)
    }
}