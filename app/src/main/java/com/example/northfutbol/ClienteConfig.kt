package com.example.northfutbol;


// 1. Creamos un enum para establecer el entorno
enum class Entorno {
    NORMAL, REMOTO
}

object ClienteConfig {
    /**
     * CONFIGURACIÓN DEL CLIENTE
     * ============================
     * Esta clase centraliza la configuracion de red de la app
     */

    val ENTORNO_ACTUAL = Entorno.REMOTO

    // Establecemos la IP del servidor en funcion del entorno
    fun getServerIP(): String {
        return when (ENTORNO_ACTUAL) {
            //10.0.2.2 == localhost
            Entorno.NORMAL -> "10.0.2.2"

            // IP de nuestro PC: conexión desde la
            // red o la publica desde fuera (192...)
            Entorno.REMOTO -> "192.168.1.2"
        }
    }

    // 2. Configuramos el puerto
    const val PUERTO_SERVIDOR = 5000
}
