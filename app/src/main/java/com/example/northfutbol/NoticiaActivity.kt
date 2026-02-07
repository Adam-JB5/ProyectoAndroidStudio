package com.example.northfutbol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NoticiaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_noticia)

        // TOP BAR
        setupTopBarOverlay()

        // BOTTOM BAR
        setupBottomBar(R.id.bottomBar)

        // Vistas principales
        val edtComentario = findViewById<EditText>(R.id.edtComentario)
        val btnEnviarComentario = findViewById<Button>(R.id.btnEnviarComentario)

        btnEnviarComentario.setOnClickListener {
            val comentario = edtComentario.text.toString().trim()

            if (comentario.isNotEmpty()) {
                // Aquí luego puedes:
                // - Inflar item_comentario
                // - Añadirlo a containerComentarios
                // - Guardarlo en BD / Firebase / API

                edtComentario.text.clear()
            }
        }
    }
}
