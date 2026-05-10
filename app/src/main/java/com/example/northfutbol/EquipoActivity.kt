package com.example.northfutbol

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
}
