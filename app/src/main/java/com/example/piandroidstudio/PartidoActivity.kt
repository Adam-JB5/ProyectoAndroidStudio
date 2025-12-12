package com.example.piandroidstudio

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PartidoActivity : AppCompatActivity() {

    private val jugadoresSeleccionados = mutableSetOf<Int>() // guardo id de jugadores marcados

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partido) // tu XML adaptado

        setupBottomBar(R.id.bottomBar)

        // Lista de ImageViews que funcionan como "checkbox visual"
        val iconos = listOf(
            findViewById<ImageView>(R.id.seleccion_j1),
            findViewById<ImageView>(R.id.seleccion_j2),
            findViewById<ImageView>(R.id.seleccion_j3),
            findViewById<ImageView>(R.id.seleccion_j4)
        )

        // asigno listener para hacer toggle de selección
        iconos.forEachIndexed { index, imageView ->

            imageView.setOnClickListener {

                if (jugadoresSeleccionados.contains(index)) {
                    // 🔴 desmarcar
                    jugadoresSeleccionados.remove(index)
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)

                } else {
                    // 🟢 marcar
                    jugadoresSeleccionados.add(index)
                    imageView.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Mensaje con conteo actualizado
                Toast.makeText(
                    this,
                    "Jugadores seleccionados: ${jugadoresSeleccionados.size}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
