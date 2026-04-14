package com.example.northfutbol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

//TODO en caso de exito, poner mensaje y redirigir
class EncuestaActivity : AppCompatActivity() {

    private lateinit var preguntas: Array<String>
    private lateinit var textViews: Array<TextView?>

    private var tvPregunta1: TextView? = null
    private var tvPregunta2: TextView? = null
    private var tvPregunta3: TextView? = null
    private var tvPregunta4: TextView? = null
    private var tvPregunta5: TextView? = null

    private var ratingPregunta1: RatingBar? = null
    private var ratingPregunta2: RatingBar? = null
    private var ratingPregunta3: RatingBar? = null
    private var ratingPregunta4: RatingBar? = null
    private var ratingPregunta5: RatingBar? = null

    private var etComentarios: EditText? = null
    private var btnEnviar: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encuesta)

        initViews()
        preguntas = resources.getStringArray(R.array.preguntas)
        textViews = arrayOf(tvPregunta1, tvPregunta2, tvPregunta3, tvPregunta4, tvPregunta5)
        rellenarPreguntas()

        btnEnviar?.setOnClickListener(View.OnClickListener { v: View? ->
            if (validarFormulario()) {
                
            }
            enviarCorreo()
        })
    }

    private fun initViews() {
        tvPregunta1 = findViewById(R.id.tvPregunta1)
        tvPregunta2 = findViewById(R.id.tvPregunta2)
        tvPregunta3 = findViewById(R.id.tvPregunta3)
        tvPregunta4 = findViewById(R.id.tvPregunta4)
        tvPregunta5 = findViewById(R.id.tvPregunta5)

        ratingPregunta1 = findViewById(R.id.ratingPregunta1)
        ratingPregunta2 = findViewById(R.id.ratingPregunta2)
        ratingPregunta3 = findViewById(R.id.ratingPregunta3)
        ratingPregunta4 = findViewById(R.id.ratingPregunta4)
        ratingPregunta5 = findViewById(R.id.ratingPregunta5)

        etComentarios = findViewById(R.id.etComentarios)
        btnEnviar = findViewById(R.id.btnEnviar)
    }

    private fun rellenarPreguntas() {
        for (i in preguntas.indices) {
            textViews[i]?.text = preguntas[i]
        }
    }

    // Valida que todas las preguntas estén respondidas y el email sea válido
    private fun validarFormulario(): Boolean {
        val vals = this.valoraciones
        for (i in vals.indices) {
            if (vals[i] == 0) {
                Toast.makeText(
                    this,
                    "Por favor valora la pregunta " + (i + 1),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        }
        return true
    }

    private val valoraciones: IntArray
        get() = intArrayOf(
            ratingPregunta1!!.getRating().toInt(),
            ratingPregunta2!!.getRating().toInt(),
            ratingPregunta3!!.getRating().toInt(),
            ratingPregunta4!!.getRating().toInt(),
            ratingPregunta5!!.getRating().toInt()
        )

    // Abre directamente el cliente de correo con el resumen preformateado
    private fun enviarCorreo() {
        val vals = this.valoraciones
        val comentarios = etComentarios?.getText().toString().trim { it <= ' ' }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>("NORTHFUTBOL@NF.COM")) //TODO crear y cambiar mail
        intent.putExtra(Intent.EXTRA_SUBJECT, "Resumen Encuesta de Valoración")
        intent.putExtra(Intent.EXTRA_TEXT, construirCuerpo(vals))

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Enviar con…"))
        } else {
            Toast.makeText(
                this,
                "No hay ninguna app de correo instalada. Por favor instale algún cliente de correo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun construirCuerpo(vals: IntArray): String {
        var media = 0.0
        for (v in vals) media += v.toDouble()
        media /= vals.size.toDouble()

        val sb = StringBuilder()
        sb.append("RESUMEN DE ENCUESTA DE VALORACIÓN\n")
        sb.append("==================================\n\n")

        for (i in preguntas.indices) {
            sb.append(
                String.format(
                    "%s: %d/5 %s\n\n",
                    preguntas[i], vals[i], estrellas(vals[i])
                )
            )
        }

        sb.append("\n----------------------------------\n")
        sb.append(String.format("MEDIA FINAL: %.1f / 5 %s\n", media, estrellas(media.toInt())))
        sb.append("----------------------------------\n")

        sb.append("----------------------------------\n")
        sb.append("COMENTARIOS:\n")
        sb.append(etComentarios?.text.toString() + "\n")
        sb.append("----------------------------------\n")

        return sb.toString()
    }

    private fun estrellas(valor: Int): String {
        val s = StringBuilder()
        for (i in 0 until valor) s.append("★")
        for (i in valor..4) s.append("☆")
        return s.toString()
    }
}