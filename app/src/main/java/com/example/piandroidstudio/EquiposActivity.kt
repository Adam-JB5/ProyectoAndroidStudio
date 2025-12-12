package com.example.piandroidstudio

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class EquiposActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipos)

        // ---------------- BOTTOM NAV ----------------
        val bottomNav: LinearLayout = findViewById(R.id.bottomNav)

        bottomNav.getChildAt(0).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        bottomNav.getChildAt(1).setOnClickListener { }
        bottomNav.getChildAt(2).setOnClickListener { }
        bottomNav.getChildAt(3).setOnClickListener { }

        // ----------------  GRUPOS DESPLEGABLES ----------------

        // GRUPO 1
        val btnGrupo1 = findViewById<ImageView>(R.id.btnGrupo1)
        val contGrupo1 = findViewById<LinearLayout>(R.id.containerGrupo1)
        contGrupo1.visibility = View.GONE

        btnGrupo1.setOnClickListener {
            if (contGrupo1.visibility == View.VISIBLE) {
                contGrupo1.visibility = View.GONE
                btnGrupo1.setImageResource(android.R.drawable.ic_menu_more)
            } else {
                contGrupo1.visibility = View.VISIBLE
                btnGrupo1.setImageResource(android.R.drawable.arrow_down_float)
            }
        }

        // GRUPO 2
        val btnGrupo2 = findViewById<ImageView>(R.id.btnGrupo2)
        val contGrupo2 = findViewById<LinearLayout>(R.id.containerGrupo2)
        contGrupo2.visibility = View.GONE

        btnGrupo2.setOnClickListener {
            if (contGrupo2.visibility == View.VISIBLE) {
                contGrupo2.visibility = View.GONE
                btnGrupo2.setImageResource(android.R.drawable.ic_menu_more)
            } else {
                contGrupo2.visibility = View.VISIBLE
                btnGrupo2.setImageResource(android.R.drawable.arrow_down_float)
            }
        }
    }
}
