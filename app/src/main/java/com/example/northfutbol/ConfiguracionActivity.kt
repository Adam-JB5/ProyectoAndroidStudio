package com.example.northfutbol


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Usuario

class ConfiguracionActivity : AppCompatActivity() {

    private lateinit var editNombre: EditText
    private lateinit var editEmail: EditText
    private lateinit var txtRol: TextView
    private lateinit var btnModificar: Button
    private lateinit var btnBorrar: Button
    private lateinit var imgPerfil: ImageView

    private lateinit var supabase: SupabaseClient
    private var imageUri: Uri? = null

    // Launcher para abrir la galería
    private val launcherGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                imgPerfil.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        initSupabase()
        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)
        initViews()
        cargarDatosUsuario()
        setupOverlayPerfil()
        setupBotones()
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
        editNombre = findViewById(R.id.editNombre)
        editEmail = findViewById(R.id.editEmail)
        txtRol = findViewById(R.id.txtRol)
        btnModificar = findViewById(R.id.btnModificar)
        btnBorrar = findViewById(R.id.btnBorrar)
        imgPerfil = findViewById(R.id.imgPerfil)

        // Al hacer clic en la foto, abrir galería
        imgPerfil.setOnClickListener {
            launcherGaleria.launch("image/*")
        }
    }

    private fun setupOverlayPerfil() {
        val overlayPerfil = findViewById<LinearLayout>(R.id.overlayPerfil)
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val idUsuario = prefs.getInt("idUsuario", 0)

        if (idUsuario == 0) {
            overlayPerfil.visibility = View.VISIBLE
            btnModificar.isEnabled = false
            btnBorrar.isEnabled = false
        } else {
            overlayPerfil.visibility = View.GONE
        }
    }

    private fun cargarDatosUsuario() {
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        editNombre.setText(prefs.getString("nombre", ""))
        editEmail.setText(prefs.getString("email", ""))
        txtRol.text = prefs.getString("rol", "")

        val fotoUrl = prefs.getString("foto", "")
        if (!fotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.user)
                .into(imgPerfil)
        }
    }

    private fun setupBotones() {
        btnModificar.setOnClickListener { onModificarClick() }
        btnBorrar.setOnClickListener { onBorrarClick() }
    }

    private suspend fun subirImagenSupabase(): String? {
        if (imageUri == null) return null

        return try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes() ?: return null

            val nombreArchivo = "perfil_${System.currentTimeMillis()}.jpg"

            // Usamos el bucket "usuarios" (asegúrate de que exista en tu Supabase)
            supabase.storage
                .from("usuarios")
                .upload(nombreArchivo, bytes)

            val urlPublica = supabase.storage
                .from("usuarios")
                .publicUrl(nombreArchivo)

            Log.d("ConfiguracionActivity", "Nueva URL foto: $urlPublica")
            urlPublica
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("ConfiguracionActivity", "Error al subir imagen a supabase: ${e.message}")
            null
        }
    }

    private fun onModificarClick() {
        val nuevoNombre = editNombre.text.toString()
        val nuevoEmail = editEmail.text.toString()

        if (nuevoNombre.isBlank() || nuevoEmail.isBlank()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        btnModificar.isEnabled = false
        btnModificar.text = "Guardando..."

        lifecycleScope.launch {
            // 1. Subir imagen si se ha seleccionado una nueva
            var urlImagenFinal = getSharedPreferences("usuario", MODE_PRIVATE).getString("foto", null)

            if (imageUri != null) {
                val nuevaUrl = withContext(Dispatchers.IO) { subirImagenSupabase() }
                if (nuevaUrl != null) {
                    urlImagenFinal = nuevaUrl
                }
            }

            // 2. Preparar objeto usuario y petición
            val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
            val nuevoUsuario = Usuario().apply {
                setIdUsuario(prefs.getInt("idUsuario", 0))
                setNombre(nuevoNombre)
                setEmail(nuevoEmail)
                setFotoPerfil(urlImagenFinal) // Se asume que existe setFoto en tu POJO
            }

            val peticion = PeticionUsuario(
                PeticionUsuario.TipoOperacion.UPDATE_USER_NAME_EMAIL,
                nuevoUsuario
            )

            try {
                val respuesta = withContext(Dispatchers.IO) {
                    ClienteSocketUsuario(
                        ClienteConfig.getServerIP(),
                        ClienteConfig.PUERTO_SERVIDOR
                    ).enviarPeticion(peticion)
                }

                if (respuesta?.isExito == true) {
                    prefs.edit()
                        .putString("nombre", nuevoNombre)
                        .putString("email", nuevoEmail)
                        .putString("foto", urlImagenFinal)
                        .apply()
                    Toast.makeText(this@ConfiguracionActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ConfiguracionActivity, respuesta?.mensaje ?: "Error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ConfiguracionActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                btnModificar.isEnabled = true
                btnModificar.text = "Modificar"
            }
        }
    }

    private fun onBorrarClick() {
        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)
        val idUsuario = prefs.getInt("idUsuario", 0)

        val usuarioAEliminar = Usuario().apply {
            setIdUsuario(idUsuario)
        }

        val peticion = PeticionUsuario(
            PeticionUsuario.TipoOperacion.DELETE,
            usuarioAEliminar
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val respuesta = ClienteSocketUsuario(
                    ClienteConfig.getServerIP(),
                    ClienteConfig.PUERTO_SERVIDOR
                ).enviarPeticion(peticion)

                withContext(Dispatchers.Main) {
                    if (respuesta?.isExito == true) {
                        prefs.edit().clear().apply()
                        Toast.makeText(this@ConfiguracionActivity, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                        redirigirAlLogin()
                    } else {
                        Toast.makeText(
                            this@ConfiguracionActivity,
                            respuesta?.mensaje ?: "Error al eliminar la cuenta",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConfiguracionActivity, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun redirigirAlLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}