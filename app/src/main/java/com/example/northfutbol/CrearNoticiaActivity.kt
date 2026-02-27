package com.example.northfutbol

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import pojosnorthfutbol.Equipo
import pojosnorthfutbol.Noticia

class CrearNoticiaActivity : AppCompatActivity() {

    // Declaración de variables para las vistas
    private lateinit var etTitulo: EditText
    private lateinit var etSubtitulo: EditText
    private lateinit var etContenido: EditText
    private lateinit var btnSeleccionarImagen: FrameLayout
    private lateinit var ivPreview: ImageView
    private lateinit var spinnerEquipos: Spinner
    private lateinit var btnPublicar: Button

    private lateinit var supabase: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_noticia) // Asegúrate que el XML se llame así

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        initSupabase()

        // 2. Inicializar Vistas
        initViews()

        // 3. Configurar Spinner de equipos (Ejemplo estático)
        setupSpinner()

        // 4. Configurar Listeners
        setupListeners()
    }

    private fun initSupabase() {
        supabase = createSupabaseClient(
            supabaseUrl = "https://ppavafsxbifmcfhsbscs.supabase.co",
            supabaseKey = ""
        ) {
            install(Postgrest)
            install(Storage)
        }
    }

    private fun initViews() {
        etTitulo = findViewById(R.id.etTitulo)
        etSubtitulo = findViewById(R.id.etSubtitulo)
        etContenido = findViewById(R.id.etContenido)
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen)
        ivPreview = findViewById(R.id.ivPreview)
        spinnerEquipos = findViewById(R.id.spinnerEquipos)
        btnPublicar = findViewById(R.id.btnPublicar)
    }

    private fun setupListeners() {
        // Clic en el área de imagen
        btnSeleccionarImagen.setOnClickListener {
            abrirGaleria()
        }

        // Clic en el botón publicar
        btnPublicar.setOnClickListener {
            validarYPublicar()
        }
    }

    private var imageUri: Uri? = null

    private val launcherGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                ivPreview.setImageURI(uri)
            }
        }

    private fun abrirGaleria() {
        launcherGaleria.launch("image/*")
    }

    private suspend fun subirImagenSupabase(): String? {
        if (imageUri == null) return null

        return try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes() ?: return null

            val nombreArchivo = "noticia_${System.currentTimeMillis()}.jpg"

            supabase.storage
                .from("noticias")
                .upload(nombreArchivo, bytes)

            // URL pública
            val urlPublica = supabase.storage
                .from("noticias")
                .publicUrl(nombreArchivo)

            // 🔹 Mostrar en Logcat
            Log.d("CrearNoticiaActivity", "URL de la imagen: $urlPublica")

            urlPublica

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Necesitamos un mapa para saber qué ID corresponde a cada nombre del Spinner
    private val mapaEquipos = mapOf(
        "Global" to Equipo().apply { idEquipo = 1; nombre = "Global" },
        "Equipo A" to Equipo().apply { idEquipo = 2; nombre = "Equipo A" },
        "Equipo B" to Equipo().apply { idEquipo = 3; nombre = "Equipo B" },
        "Equipo C" to Equipo().apply { idEquipo = 4; nombre = "Equipo C" }
    )

    private fun setupSpinner() {
        val nombresEquipos = mapaEquipos.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresEquipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEquipos.adapter = adapter
    }

    private fun validarYPublicar() {
        val titulo = etTitulo.text.toString().trim()
        val contenido = etContenido.text.toString().trim()
        val subtitulo = etSubtitulo.text.toString().trim()

        // Obtenemos el nombre seleccionado en el Spinner
        val nombreSeleccionado = spinnerEquipos.selectedItem.toString()

        // Buscamos el objeto Equipo completo en nuestro mapa
        val equipoSeleccionado = mapaEquipos[nombreSeleccionado]

        if (titulo.isEmpty() || contenido.isEmpty() || imageUri == null || equipoSeleccionado == null) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        btnPublicar.isEnabled = false
        btnPublicar.text = "Subiendo noticia..."

        lifecycleScope.launch {
            val urlImagen = withContext(Dispatchers.IO) { subirImagenSupabase() }

            if (urlImagen != null) {
                // Creamos la noticia
                val noticia = Noticia().apply {
                    this.titulo = titulo
                    this.subtitulo = if (subtitulo.isEmpty()) null else subtitulo
                    this.contenido = contenido
                    this.imagen = urlImagen
                    // IMPORTANTE: Aquí pasamos el objeto equipo completo
                    this.equipo = equipoSeleccionado
                }

                enviarNoticiaAlServidor(noticia)
            } else {
                restaurarBoton()
                Toast.makeText(this@CrearNoticiaActivity, "Error con la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun enviarNoticiaAlServidor(noticia: Noticia) {
        withContext(Dispatchers.IO) {
            try {
                // Creamos la petición similar a tu registro de usuario
                val peticion = PeticionNoticia(
                    PeticionNoticia.TipoOperacion.CREATE,
                    noticia
                )

                val respuesta = ClienteSocketNoticia(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true) {
                        Toast.makeText(this@CrearNoticiaActivity, "Noticia creada con éxito", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CrearNoticiaActivity, "Error: ${respuesta?.mensaje}", Toast.LENGTH_SHORT).show()
                        restaurarBoton()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CrearNoticiaActivity, "Servidor no disponible", Toast.LENGTH_SHORT).show()
                    restaurarBoton()
                }
            }
        }
    }

    private fun restaurarBoton() {
        btnPublicar.isEnabled = true
        btnPublicar.text = "PUBLICAR NOTICIA"
    }
}