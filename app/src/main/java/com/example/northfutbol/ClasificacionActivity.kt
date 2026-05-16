package com.example.northfutbol

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

        val peticion = PeticionClasificacion(PeticionClasificacion.TipoOperacion.READ_BY_GRUPO, grupo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketClasificacion(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    Log.d("DEBUG_CLASIFICACION", "Exito: ${respuesta?.isExito}")
                    Log.d("DEBUG_CLASIFICACION", "Equipos: ${respuesta?.equipos?.size ?: 0}")

                    if (respuesta?.isExito == true && !respuesta.equipos.isNullOrEmpty()) {
                        respuesta.equipos.forEachIndexed { index, equipo ->
                            inflarFila(equipo, index + 1, respuesta.equipos.size)
                        }
                    } else {
                        Toast.makeText(
                            this@ClasificacionActivity,
                            "No hay datos para este grupo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al cargar clasificación: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ClasificacionActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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

        val indicador = fila.findViewById<View>(R.id.indicadorPosicion)
        when {
            posicion <= 2                -> indicador.setBackgroundColor(Color.parseColor("#2196F3"))
            posicion <= 4                -> indicador.setBackgroundColor(Color.parseColor("#4CAF50"))
            posicion >= totalEquipos - 2 -> indicador.setBackgroundColor(Color.parseColor("#F44336"))
            else                         -> indicador.setBackgroundColor(Color.TRANSPARENT)
        }

        contenedorClasificacion.addView(fila)
    }
}