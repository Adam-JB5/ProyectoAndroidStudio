package com.example.northfutbol

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pojosnorthfutbol.Usuario

class RegisterDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.dialog_register, container, false)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val txtBackToLogin = view.findViewById<TextView>(R.id.txtBackToLogin)

        val inputNombre = view.findViewById<EditText>(R.id.inputNombre)
        val inputEmail = view.findViewById<EditText>(R.id.inputRegisterEmail)
        val inputPassword = view.findViewById<EditText>(R.id.inputRegisterPassword)

        btnRegister.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            //TODO cambiar a isBlank() la validacion
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Registrando..."

            // 1️⃣ Crear usuario con los datos
            val nuevoUsuario = Usuario().apply {
                setNombre(nombre)
                setEmail(email)
                setContrasenna(password)   // TODO hashear la contrasenna
                setRol("R")                // Rol por defecto
                setFotoPerfil(null)
            }

            // 2️⃣ Crear petición REGISTER
            val peticion = Peticion(
                Peticion.TipoOperacion.REGISTER,
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
                                context,
                                "Usuario registrado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            dismiss()

                        } else {
                            Toast.makeText(
                                context,
                                respuesta?.mensaje ?: "No se pudo registrar el usuario",
                                Toast.LENGTH_SHORT
                            ).show()

                            restaurarBoton(btnRegister)
                        }
                    }

                } catch (e: Exception) {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Servidor no disponible", Toast.LENGTH_SHORT).show()
                        restaurarBoton(btnRegister)
                    }


                }
            }

        }

        txtBackToLogin.setOnClickListener {
            val loginDialog = LoginDialogFragment()
            loginDialog.show(parentFragmentManager, "loginDialog")
            dismiss()
        }

        return view
    }

    private fun restaurarBoton(btn: Button) {
        btn.isEnabled = true
        btn.text = "Registrarse"
    }

    override fun onStart() {
        super.onStart()
        // Mantiene el mismo diseño estético que el login
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}