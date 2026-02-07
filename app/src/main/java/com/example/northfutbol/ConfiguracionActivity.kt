package com.example.northfutbol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ConfiguracionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        setupTopBarOverlay()
        setupBottomBar(R.id.bottomBar)
    }
}
