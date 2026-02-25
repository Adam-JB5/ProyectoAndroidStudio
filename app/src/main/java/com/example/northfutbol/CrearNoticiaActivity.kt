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
            supabaseKey = "sb_secret_mNFmA2UO4WRHkGpnLojsqQ_Jyv6ajW3"
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

    private fun setupSpinner() {
        // Lista de ejemplo para el Spinner
        val equipos = listOf("Global", "Equipo A", "Equipo B", "Equipo C")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, equipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEquipos.adapter = adapter
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

    private suspend fun guardarNoticiaBD(urlImagen: String) {
        val noticia = mapOf(
            "titulo" to etTitulo.text.toString(),
            "contenido" to etContenido.text.toString(),
            "imagen_url" to urlImagen
        )

        supabase.from("noticias").insert(noticia)

        runOnUiThread {
            Toast.makeText(this, "Noticia publicada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validarYPublicar() {
        val titulo = etTitulo.text.toString().trim()
        val contenido = etContenido.text.toString().trim()

        if (titulo.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(this, "Campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val urlImagen = withContext(Dispatchers.IO) {
                subirImagenSupabase()
            }

            if (urlImagen != null) {
                guardarNoticiaBD(urlImagen)
            } else {
                Toast.makeText(this@CrearNoticiaActivity, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}