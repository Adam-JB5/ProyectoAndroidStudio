package com.example.piandroidstudio

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate que el nombre del layout sea activity_main.xml

        //BOTTOM BAR
        setupBottomBar(R.id.bottomBar)

        // TOP BAR
        val btnUsuario: ImageView = findViewById(R.id.btnUsuario)
        val btnMenu: ImageView = findViewById(R.id.btnMenu)

        btnUsuario.setOnClickListener {
            Toast.makeText(this, "Usuario clickeado", Toast.LENGTH_SHORT).show()
        }

        btnMenu.setOnClickListener {
            Toast.makeText(this, "Menú clickeado", Toast.LENGTH_SHORT).show()
        }

        // PARTIDOS HORIZONTALES
        val btnPartido1: Button = findViewById(R.id.btnPartido1)
        val btnPartido2: Button = findViewById(R.id.btnPartido2)
        val btnPartido3: Button = findViewById(R.id.btnPartido3)

        btnPartido1.setOnClickListener {
            Toast.makeText(this, "Partido 1 clickeado", Toast.LENGTH_SHORT).show()
        }
        btnPartido2.setOnClickListener {
            Toast.makeText(this, "Partido 2 clickeado", Toast.LENGTH_SHORT).show()
        }
        btnPartido3.setOnClickListener {
            Toast.makeText(this, "Partido 3 clickeado", Toast.LENGTH_SHORT).show()
        }

        // NEWS ITEM
        val newsItem1: LinearLayout = findViewById(R.id.newsItem1)
        newsItem1.setOnClickListener {
            Toast.makeText(this, "Noticia clickeada", Toast.LENGTH_SHORT).show()
        }


    }
}
