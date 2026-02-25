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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Noticia

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

        // Lógica de partidos (puedes hacer lo mismo que con noticias luego)
        setupClickPartidosEstaticos()
    }

    private fun cargarNoticiasDesdeServidor() {
        val contenedorNoticias = findViewById<LinearLayout>(R.id.contenedorNoticias)

        // Limpiamos los <include> estáticos que pusiste en el XML
        contenedorNoticias.removeAllViews()

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

        // Si usas Glide o Picasso para fotos:
        // Glide.with(this).load(noticia.urlImagen).into(imagen)

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
        val contenedorPartidos = findViewById<LinearLayout>(R.id.contenedorPartidos)
        for (i in 0 until contenedorPartidos.childCount) {
            contenedorPartidos.getChildAt(i).setOnClickListener {
                startActivity(Intent(this, PartidoActivity::class.java))
            }
        }
    }
}
