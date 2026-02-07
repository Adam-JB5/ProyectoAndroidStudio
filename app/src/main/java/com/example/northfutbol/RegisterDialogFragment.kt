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

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            } else {
                // AQUÍ LA LÓGICA PARA TU TABLA "USUARIO"
                // 1. ROL: Siempre enviar "U" por defecto
                val rol = "R"

                // 2. PASSWORD: Aquí deberías aplicar un hash SHA-256
                // para que coincida con tu columna CHAR(64)

                // TODO: Llamada a tu API o DatabaseHelper para el INSERT

                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        txtBackToLogin.setOnClickListener {
            val loginDialog = LoginDialogFragment()
            loginDialog.show(parentFragmentManager, "loginDialog")
            dismiss()
        }

        return view
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