package com.example.northfutbol

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NoticiaActivity : AppCompatActivity() {

    private var idNoticia: Int = -1

    // Vistas
    private lateinit var imgNoticia: ImageView
    private lateinit var txtTitulo: TextView
    private lateinit var txtSubtitulo: TextView
    private lateinit var txtEquipo: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtContenido: TextView
    private lateinit var edtComentario: EditText
    private lateinit var btnEnviarComentario: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noticia)

        // 1. Obtener ID del Intent
        idNoticia = intent.getIntExtra("ID_NOTICIA", -1)

        // TOP / BOTTOM BAR
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        // 2. Inicializar Vistas
        initViews()

        // 3. Cargar datos si el ID es válido
        if (idNoticia != -1) {
            obtenerDatosNoticia()
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la noticia", Toast.LENGTH_SHORT).show()
        }

        // 4. Lógica de comentarios
        btnEnviarComentario.setOnClickListener {
            val comentario = edtComentario.text.toString().trim()
            if (comentario.isNotEmpty()) {
                // TODO: Implementar envío de comentario al servidor
                Toast.makeText(this, "Comentario enviado (Simulado)", Toast.LENGTH_SHORT).show()
                edtComentario.text.clear()
            }
        }
    }

    private fun initViews() {
        imgNoticia = findViewById(R.id.imgNoticia)
        txtTitulo = findViewById(R.id.txtTitulo)
        txtSubtitulo = findViewById(R.id.txtSubtitulo)
        txtEquipo = findViewById(R.id.txtEquipo)
        txtFecha = findViewById(R.id.txtFecha)
        txtContenido = findViewById(R.id.txtContenido)
        edtComentario = findViewById(R.id.edtComentario)
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario)
    }

    private fun obtenerDatosNoticia() {
        // Creamos la petición de tipo READ con el ID
        val peticion = PeticionNoticia(PeticionNoticia.TipoOperacion.READ, idNoticia)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketNoticia(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true && respuesta.noticia != null) {
                        val noticia = respuesta.noticia
                        
                        // Rellenar los campos de la UI
                        txtTitulo.text = noticia.titulo
                        txtSubtitulo.text = noticia.subtitulo
                        txtContenido.text = noticia.contenido
                        txtEquipo.text = noticia.equipo?.nombre ?: "General"
                        noticia.fechaCreacion?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            txtFecha.text = sdf.format(it)
                        }

                        // Cargar Imagen con Glide
                        Glide.with(this@NoticiaActivity)
                            .load(noticia.imagen)
                            .placeholder(R.color.negro_secciones)
                            .error(android.R.drawable.ic_menu_report_image)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop()
                            .into(imgNoticia)
                    } else {
                        Toast.makeText(this@NoticiaActivity, "No se pudo cargar la noticia", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_SERVER", "Error al obtener noticia: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NoticiaActivity, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
