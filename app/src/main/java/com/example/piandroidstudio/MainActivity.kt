package com.example.piandroidstudio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate que el nombre del layout sea activity_main.xml

        //TOP BAR
        setupTopBarOverlay()
        //BOTTOM BAR
        setupBottomBar(R.id.bottomBar)


    }
}
