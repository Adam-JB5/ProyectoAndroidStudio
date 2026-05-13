package com.example.northfutbol

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

class ZigzagBackground(
    private val colorLeft: Int = Color.parseColor("#1a7abf"),
    private val colorRight: Int = Color.parseColor("#3aab5e")
) : Drawable() {

    private val paintLeft = Paint().apply { color = colorLeft; isAntiAlias = true }
    private val paintRight = Paint().apply { color = colorRight; isAntiAlias = true }

    override fun draw(canvas: Canvas) {
        val w = bounds.width().toFloat()
        val h = bounds.height().toFloat()
        val zigSize = 24f
        val amplitude = 20f

        // Fondo derecho completo
        canvas.drawRect(0f, 0f, w, h, paintRight)

        // Lado izquierdo con zigzag diagonal
        val path = Path().apply {
            moveTo(0f, 0f)

            // La línea de separación empieza en ~40% arriba y termina en ~60% abajo (diagonal)
            val xStart = w * 0.35f
            val xEnd = w * 0.55f

            lineTo(xStart, 0f)

            val steps = (h / zigSize).toInt() + 1
            for (i in 0..steps) {
                val y = i * zigSize
                // Interpolamos x linealmente para la diagonal
                val xBase = xStart + (xEnd - xStart) * (y / h)
                val xZig = if (i % 2 == 0) xBase + amplitude else xBase - amplitude
                lineTo(xZig, y)
            }

            lineTo(0f, h)
            close()
        }
        canvas.drawPath(path, paintLeft)
    }

    override fun setAlpha(alpha: Int) { paintLeft.alpha = alpha; paintRight.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { paintLeft.colorFilter = cf; paintRight.colorFilter = cf }
    override fun getOpacity() = PixelFormat.OPAQUE
}