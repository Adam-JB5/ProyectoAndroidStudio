package com.example.northfutbol

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Comentario
import pojosnorthfutbol.Noticia
import pojosnorthfutbol.Usuario
import java.text.SimpleDateFormat
import java.util.Date
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
    private lateinit var containerComentarios: LinearLayout

    // Datos
    private var usuarioActualID: Int = -1

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
            obtenerComentarios()
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la noticia", Toast.LENGTH_SHORT).show()
        }

        // 4. Lógica de comentarios
        btnEnviarComentario.setOnClickListener {
            val textoComentario = edtComentario.text.toString().trim()
            if (textoComentario.isNotEmpty()) {
                enviarComentario(textoComentario)
            } else {
                Toast.makeText(this, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
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
        containerComentarios = findViewById(R.id.containerComentarios)
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

    private fun obtenerComentarios() {
        val peticion = PeticionComentario()
        peticion.tipoOperacion = PeticionComentario.TipoOperacion.READ_BY_NOTICIA
        peticion.idNoticia = idNoticia
        
        Log.d("DEBUG_COMENTARIOS", "Enviando petición READ_BY_NOTICIA con idNoticia=$idNoticia")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketComentario(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta != null && !respuesta.comentarios.isNullOrEmpty()) {
                        Log.d("DEBUG_COMENTARIOS", "Comentarios cargados: ${respuesta.comentarios.size}")
                        mostrarComentarios(respuesta.comentarios)
                    } else {
                        Log.d("DEBUG_COMENTARIOS", "Sin comentarios - isExito: ${respuesta?.isExito}, lista: ${respuesta?.comentarios}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_COMENTARIOS", "Error al obtener comentarios: ${e.message}")
            }
        }
    }

    private fun mostrarComentarios(comentarios: List<Comentario>) {
        containerComentarios.removeAllViews()
        
        if (comentarios.isEmpty()) {
            val tvSinComentarios = TextView(this)
            tvSinComentarios.text = "No hay comentarios aún"
            tvSinComentarios.setTextColor(android.graphics.Color.parseColor("#888888"))
            tvSinComentarios.textSize = 14f
            containerComentarios.addView(tvSinComentarios)
        } else {
            for (comentario in comentarios) {
                agregarComentarioAUI(comentario)
            }
        }
    }

    private fun agregarComentarioAUI(comentario: Comentario) {
        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.item_comentario, containerComentarios, false)

        itemView.findViewById<TextView>(R.id.txtUsuarioComentario).text =
            comentario.usuario?.nombre ?: "Usuario"

        // Imagen del usuario del comentario
        val imgUsuario = itemView.findViewById<ImageView>(R.id.imgPerfilComentario)
        val fotoUrl = comentario.usuario?.fotoPerfil
        if (!fotoUrl.isNullOrEmpty()) {
            Log.d("DEBUG_FOTO", "Cargando foto de perfil: $fotoUrl")
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(imgUsuario)
        } else {
            Log.d("DEBUG_FOTO", "No se cargó la foto de perfil: $fotoUrl")
            imgUsuario.setImageResource(R.drawable.user)
        }

        itemView.findViewById<TextView>(R.id.txtContenidoComentario).text = comentario.contenido
        comentario.fechaCreacion?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            itemView.findViewById<TextView>(R.id.txtFechaComentario).text = sdf.format(it)
        }

        // Solo una declaración de prefs
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val idUsuarioSesion = prefs.getInt("idUsuario", -1)
        val rolUsuarioSesion = prefs.getString("rol", "")

        val esAdmin = rolUsuarioSesion == "A"
        val esAutor = comentario.usuario?.idUsuario == idUsuarioSesion

        val btnEliminar = itemView.findViewById<ImageView>(R.id.btnEliminarComentario)
        if (esAdmin || esAutor) {
            btnEliminar.visibility = View.VISIBLE
            btnEliminar.setOnClickListener {
                confirmarEliminarComentario(comentario, itemView)
            }
        }

        containerComentarios.addView(itemView)
    }

    private fun eliminarComentario(comentario: Comentario, itemView: View) {
        val peticion = PeticionComentario().apply {
            tipoOperacion = PeticionComentario.TipoOperacion.DELETE
            this.idComentario = comentario.getIdComentario()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketComentario(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true) {
                        containerComentarios.removeView(itemView)
                        Toast.makeText(this@NoticiaActivity, "Comentario eliminado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@NoticiaActivity, "No se pudo eliminar: ${respuesta?.mensaje}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_ELIMINAR", "Error al eliminar comentario: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NoticiaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmarEliminarComentario(comentario: Comentario, itemView: View) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar comentario")
            .setMessage("¿Estás seguro de que quieres eliminar este comentario?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarComentario(comentario, itemView)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarComentario(texto: String) {
        // Obtener usuario desde SharedPreferences
        val prefs = getSharedPreferences("usuario", 0)
        val idUsuario = prefs.getInt("idUsuario", -1)
        
        if (idUsuario == -1) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Crear objeto Usuario
        val usuario = Usuario()
        usuario.idUsuario = idUsuario
        usuario.nombre = prefs.getString("nombre", "Usuario") ?: "Usuario"
        
        // Crear objeto Noticia con el ID
        val noticia = Noticia()
        noticia.idNoticia = idNoticia
        
        // Crear comentario
        val nuevoComentario = Comentario()
        nuevoComentario.contenido = texto
        nuevoComentario.usuario = usuario
        nuevoComentario.noticia = noticia
        nuevoComentario.fechaCreacion = Date()
        
        val peticion = PeticionComentario()
        peticion.tipoOperacion = PeticionComentario.TipoOperacion.CREATE
        peticion.comentario = nuevoComentario

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketComentario(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta != null && respuesta.isExito) {
                        // Agregar el comentario localmente a la UI de inmediato
                        nuevoComentario.fechaCreacion = Date()
                        agregarComentarioAUI(nuevoComentario)
                        
                        edtComentario.text.clear()
                        Toast.makeText(this@NoticiaActivity, "Comentario enviado", Toast.LENGTH_SHORT).show()
                        Log.d("DEBUG_ENVIO", "Comentario enviado correctamente, agregado a UI")
                        
                        // Recargar la lista en background para sincronizar
                        obtenerComentarios()
                    } else if (respuesta != null && respuesta.comentario != null) {
                        // Si devuelve el objeto comentario directamente
                        agregarComentarioAUI(respuesta.comentario)
                        edtComentario.text.clear()
                        Toast.makeText(this@NoticiaActivity, "Comentario enviado", Toast.LENGTH_SHORT).show()
                        Log.d("DEBUG_ENVIO", "Comentario enviado correctamente (con objeto)")
                    } else {
                        Toast.makeText(this@NoticiaActivity, "Error al enviar comentario: ${respuesta?.mensaje}", Toast.LENGTH_SHORT).show()
                        Log.d("DEBUG_ENVIO", "Error: isExito=${respuesta?.isExito}, comentario=${respuesta?.comentario}, mensaje=${respuesta?.mensaje}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ERROR_COMENTARIO", "Error al enviar comentario: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@NoticiaActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
