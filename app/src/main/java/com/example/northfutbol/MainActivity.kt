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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Noticia
import pojosnorthfutbol.Partido

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate que el nombre del layout sea activity_main.xml

        //TOP BAR
        setupTopBarOverlay()
        //BOTTOM BAR
        setupBottomBar(R.id.bottomBar)

        // 1. Cargar noticias desde el servidor
        cargarNoticiasDesdeServidor()

        cargarPartidosDesdeServidor()

        // Lógica de partidos (puedes hacer lo mismo que con noticias luego)
        setupClickPartidosEstaticos()
    }

    private fun cargarNoticiasDesdeServidor() {
        val contenedorNoticias = findViewById<LinearLayout>(R.id.contenedorNoticias)

        val peticion = PeticionNoticia(PeticionNoticia.TipoOperacion.READ_ALL, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketNoticia(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    Log.d("DEBUG_APP", "Exito: ${respuesta?.isExito}")
                    Log.d("DEBUG_APP", "Cantidad noticias: ${respuesta?.noticias?.size ?: 0}")
                    if (respuesta?.isExito == true && respuesta.noticias != null) {
                        // Iteramos sobre la lista de noticias recibida
                        for (noticia in respuesta.noticias) {
                            agregarNoticiaAVista(contenedorNoticias, noticia)
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No hay noticias disponibles",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // Esto imprimirá el error en rojo en la pestaña Logcat
                Log.e("ERROR_SERVER", "Error al obtener noticias: ${e.message}")
                e.printStackTrace() // Imprime toda la lista de llamadas para ver la línea exacta del fallo

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    private fun cargarPartidosDesdeServidor() {
        val layoutSeguidos: LinearLayout = findViewById(R.id.layoutPartidosSeguidos)

        // Igual que en ConfiguracionActivity: leemos el ID de sesión desde SharedPreferences
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val idUsuario = prefs.getInt("idUsuario", 0)

        val peticionSeguidos = PeticionPartido(PeticionPartido.TipoOperacion.READ_BY_FOLLOWED, idUsuario)

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val respuestaSeguidos = ClienteSocketPartido(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticionSeguidos)

                withContext(Dispatchers.Main) {

                    // Partidos de equipos seguidos (ya filtrados en el servidor)
                    if (respuestaSeguidos?.isExito == true && respuestaSeguidos.partidos != null) {
                        layoutSeguidos.removeAllViews()
                        respuestaSeguidos.partidos.forEach { partido ->
                            agregarPartidoAVista(layoutSeguidos, partido)
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "No sigues ningún equipo todavía",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener partidos: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
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

    private fun agregarNoticiaAVista(contenedor: LinearLayout, noticia: Noticia) {
        // Inflamos el layout item_noticia.xml individualmente
        val inflater = LayoutInflater.from(this)
        val viewNoticia = inflater.inflate(R.layout.item_noticia, contenedor, false)

        // Referencias a los elementos dentro de item_noticia
        val titulo =
            viewNoticia.findViewById<TextView>(R.id.txtTituloNoticia) // Asegúrate que estos IDs existan en item_noticia.xml
        val subtitulo = viewNoticia.findViewById<TextView>(R.id.txtSubtituloNoticia)
        val imagen = viewNoticia.findViewById<ImageView>(R.id.imgNoticia)

        // Asignamos datos
        titulo.text = noticia.titulo
        subtitulo.text = noticia.subtitulo
        Glide.with(this)
            .load(noticia.imagen)
            .placeholder(R.color.negro_secciones) // Un color sólido mientras carga
            .error(android.R.drawable.ic_menu_report_image) // Icono si la URL falla
            .transition(DrawableTransitionOptions.withCrossFade()) // Efecto de difuminado al aparecer
            .centerCrop() // Asegura que llene el cuadrado de 60dp sin deformarse
            .into(imagen)

        // Evento de clic para abrir la noticia completa
        viewNoticia.setOnClickListener {
            val intent = Intent(this, NoticiaActivity::class.java)
            intent.putExtra(
                "ID_NOTICIA",
                noticia.idNoticia
            ) // Pasamos el ID para que la otra activity sepa qué cargar
            startActivity(intent)
        }

        // Añadimos la vista inflada al contenedor principal
        contenedor.addView(viewNoticia)
    }

    private fun setupClickPartidosEstaticos() {
        val contenedorPartidos = findViewById<LinearLayout>(R.id.layoutPartidosSeguidos)
        for (i in 0 until contenedorPartidos.childCount) {
            contenedorPartidos.getChildAt(i).setOnClickListener {
                startActivity(Intent(this, PartidoActivity::class.java))
            }
        }
    }
}
