package com.example.northfutbol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Usuario

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)

        val prefs = getSharedPreferences("usuario", MODE_PRIVATE)

        val editNombre = findViewById<EditText>(R.id.editNombre)
        val editEmail = findViewById<EditText>(R.id.editEmail)
        val txtRol = findViewById<TextView>(R.id.txtRol)
        val btnModificar = findViewById<Button>(R.id.btnModificar)
        val btnBorrar = findViewById<Button>(R.id.btnBorrar)

        // Cargar datos
        editNombre.setText(prefs.getString("nombre", ""))
        editEmail.setText(prefs.getString("email", ""))
        txtRol.text = prefs.getString("rol", "")

        //TODO annadir actualizacion de la foto de perfil
        // BOTÓN MODIFICAR
        btnModificar.setOnClickListener {

            val nuevoNombre = editNombre.text.toString()
            val nuevoEmail = editEmail.text.toString()

            if (nuevoNombre.isBlank() || nuevoEmail.isBlank()) {
                Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Aquí deberías enviar también al servidor la modificación
            // 1️⃣ Crear usuario con los datos
            val nuevoUsuario = Usuario().apply {
                setIdUsuario(prefs.getInt("idUsuario", 0))
                setNombre(nuevoNombre)
                setEmail(nuevoEmail)
                //TODO annadir actualizacion de la foto de perfil
                //setFotoPerfil(null)
            }

            // 2️⃣ Crear petición REGISTER
            val peticion = PeticionUsuario(
                PeticionUsuario.TipoOperacion.UPDATE_USER_NAME_EMAIL,
                nuevoUsuario
            )

            // 3️⃣ Enviar petición al backend (igual que login)
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val respuesta = ClienteSocket(
                        ClienteConfig.getServerIP(),
                        ClienteConfig.PUERTO_SERVIDOR
                    ).enviarPeticion(peticion)

                    withContext(Dispatchers.Main) {

                        if (respuesta?.isExito == true) {

                            Toast.makeText(
                                this@ConfiguracionActivity,
                                "Usuario modificado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            prefs.edit()
                                .putString("nombre", nuevoNombre)
                                .putString("email", nuevoEmail)
                                //TODO annadir actualizacion de la foto de perfil
                                //.putString("fotoPerfil", nuevaFoto)
                                .apply()

                            Toast.makeText(this@ConfiguracionActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(
                                this@ConfiguracionActivity,
                                respuesta?.mensaje ?: "No se pudo registrar el usuario",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ConfiguracionActivity, "Servidor no disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // BOTÓN BORRAR
        btnBorrar.setOnClickListener {
            // 🔥 Aquí deberías enviar petición DELETE al servidor
            // 🔥 Y redirigir al login
            val idUsuario = prefs.getInt("idUsuario", 0)

            // 1️⃣ Crear objeto Usuario con el ID para identificar qué borrar
            val usuarioAEliminar = Usuario().apply {
                setIdUsuario(idUsuario)
            }

            // 2️⃣ Crear la petición DELETE (asegúrate de que DELETE_USER existe en tu enum TipoOperacion)
            val peticion = PeticionUsuario(
                PeticionUsuario.TipoOperacion.DELETE,
                usuarioAEliminar
            )

            // 3️⃣ Ejecutar en segundo plano
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val respuesta = ClienteSocket(
                        ClienteConfig.getServerIP(),
                        ClienteConfig.PUERTO_SERVIDOR
                    ).enviarPeticion(peticion)

                    withContext(Dispatchers.Main) {
                        if (respuesta?.isExito == true) {
                            // 4️⃣ Limpiar datos locales
                            prefs.edit().clear().apply()

                            Toast.makeText(this@ConfiguracionActivity, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()

                            // 5️⃣ Redirigir al Login y cerrar todas las actividades previas
                            val intent =
                                Intent(this@ConfiguracionActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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
        
    }
}
