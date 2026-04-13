package com.example.northfutbol

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Equipo
import kotlin.jvm.java

class EquiposActivity : AppCompatActivity() {

    private lateinit var tabsGrupo: List<TextView>
    private lateinit var contenedorEquipos: LinearLayout
    private var grupoActivo = '1'



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipos)
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        contenedorEquipos = findViewById(R.id.contenedorEquipos)

        inicializarSelectorGrupos()
        cargarGrupo(grupoActivo)
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
            val grupo = ('1' + index)  // A, B, C, D, E
            tab.setOnClickListener {
                grupoActivo = grupo
                actualizarTabActivo(grupo)  // también actualiza firma
                cargarGrupo(grupo)
            }
        }

        actualizarTabActivo('1')
    }

    private fun actualizarTabActivo(grupoSeleccionado: Char) {
        tabsGrupo.forEachIndexed { index, tab ->
            if ('1' + index == grupoSeleccionado) {  // ✅ compara por código char
                tab.setBackgroundResource(R.drawable.bg_tab_selected)
                tab.setTextColor(getColor(R.color.blanco))
            } else {
                tab.setBackgroundColor(Color.TRANSPARENT)
                tab.setTextColor(getColor(R.color.gris_texto))
            }
        }
    }

    private fun cargarGrupo(grupo: Char) {
        cargarEquiposDesdeServidor(grupo)
    }

    private fun cargarEquiposDesdeServidor(grupo: Char) {
        val peticion = PeticionEquipo(PeticionEquipo.TipoOperacion.READ_BY_GROUP, grupo)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketEquipo(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    Log.d("DEBUG_APP", "Exito equipos: ${respuesta?.isExito}")
                    Log.d("DEBUG_APP", "Cantidad equipos: ${respuesta?.equipos?.size ?: 0}")

                    contenedorEquipos.removeAllViews()

                    if (respuesta?.isExito == true && respuesta.equipos != null) {
                        for (equipo in respuesta.equipos) {
                            agregarEquipoAVista(equipo)
                        }
                    } else {
                        Toast.makeText(
                            this@EquiposActivity,
                            "No hay equipos disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener equipos: ${e.message}")
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@EquiposActivity,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun agregarEquipoAVista(equipo: Equipo) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_equipo, contenedorEquipos, false)

        view.findViewById<TextView>(R.id.txt_nombre_equipo).text = equipo.nombre
        // Si tienes más campos, añádelos aquí:
        // view.findViewById<TextView>(R.id.txt_grupo).text = "Grupo ${equipo.grupo}"

        view.setOnClickListener {
            val intent = Intent(this, EquipoActivity::class.java)
            intent.putExtra("equipo_id", equipo.idEquipo) // pasa el ID u otros datos necesarios
            startActivity(intent)
        }

        contenedorEquipos.addView(view)
    }
}