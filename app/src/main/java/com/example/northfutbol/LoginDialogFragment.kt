package com.example.northfutbol

import android.content.Intent
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

class LoginDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_login, container, false)

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val txtSwitchRegister = view.findViewById<TextView>(R.id.txtSwitchRegister)

        btnLogin.setOnClickListener {
            val email = view.findViewById<EditText>(R.id.inputEmail).text.toString()
            val password = view.findViewById<EditText>(R.id.inputPassword).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {

                // 1. Creamos un usuario "temporal" con las credenciales
                val loginDatos = Usuario().apply {
                    setEmail(email)
                    setContrasenna(HashHelper.sha256(password))
                }

                // 2. Peticion de tipo LOGIN
                val peticion = PeticionUsuario(
                    PeticionUsuario.TipoOperacion.LOGIN,
                    loginDatos
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val respuesta = ClienteSocketUsuario(
                            ClienteConfig.getServerIP(),
                            ClienteConfig.PUERTO_SERVIDOR
                        )
                            .enviarPeticion(peticion)

                        withContext(Dispatchers.Main) {
                            if (respuesta?.isExito == true) {

                                // Guardamos en SharedPreferences los datos del usuario
                                // 🔥 GUARDAR USUARIO EN SHARED PREFERENCES
                                val prefs = requireContext().getSharedPreferences("usuario", 0)
                                val editor = prefs.edit()

                                editor.putInt("idUsuario", respuesta.usuario.idUsuario)
                                editor.putString("nombre", respuesta.usuario.nombre)
                                editor.putString("email", respuesta.usuario.email)
                                editor.putString("rol", respuesta.usuario.rol)
                                editor.putString("fotoPerfil", respuesta.usuario.fotoPerfil)
                                editor.apply()

                                Toast.makeText(context, "¡Bienvenido, ${respuesta.usuario.nombre}!", Toast.LENGTH_SHORT).show()
                                dismiss()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            } else {
                                Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Servidor no disponible", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


        }

        txtSwitchRegister.setOnClickListener {
            val registerDialog = RegisterDialogFragment()
            registerDialog.show(parentFragmentManager, "registerDialog")
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}