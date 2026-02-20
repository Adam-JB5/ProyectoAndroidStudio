package com.example.northfutbol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

        // BOTÓN MODIFICAR
        btnModificar.setOnClickListener {

            val nuevoNombre = editNombre.text.toString()
            val nuevoEmail = editEmail.text.toString()

            // 🔥 Aquí deberías enviar también al servidor la modificación


            prefs.edit()
                .putString("nombre", nuevoNombre)
                .putString("email", nuevoEmail)
                .apply()

            Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show()


        }

        // BOTÓN BORRAR
        btnBorrar.setOnClickListener {
            // 🔥 Aquí deberías enviar petición DELETE al servidor
            // 🔥 Y redirigir al login


            prefs.edit().clear().apply()

            Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()


        }
    }
}
