package com.example.piandroidstudio

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate que el nombre del layout sea activity_main.xml

        //TOP BAR
        setupTopBarOverlay()
        //BOTTOM BAR
        setupBottomBar(R.id.bottomBar)


        val contenedorPartidos = findViewById<LinearLayout>(R.id.contenedorPartidos)

        for (i in 0 until contenedorPartidos.childCount) {
            val partidoView = contenedorPartidos.getChildAt(i)

            partidoView.isClickable = true
            partidoView.setOnClickListener {
                startActivity(Intent(this, PartidoActivity::class.java))
            }
        }


        val contenedorNoticias = findViewById<LinearLayout>(R.id.contenedorNoticias)

        for (i in 0 until contenedorNoticias.childCount) {
            contenedorNoticias.getChildAt(i).setOnClickListener {
                startActivity(Intent(this, NoticiaActivity::class.java))
            }
        }

    }
}
