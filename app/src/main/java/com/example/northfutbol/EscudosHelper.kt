package com.example.northfutbol

/**
 * Helper para mapear nombres de equipos a sus escudos (drawables)
 * Esta solución NO requiere campo en BD - los escudos se cargan desde resources
 * Grupo 1 - Segunda División B/Tercera División
 */
object EscudosHelper {
    
    /**
     * Retorna el ID del drawable del escudo según el nombre del equipo
     * @param nombreEquipo Nombre del equipo
     * @return ID del drawable o R.drawable.team (icono por defecto)
     */
    fun obtenerEscudo(nombreEquipo: String?): Int {
        if (nombreEquipo == null) return R.drawable.team
        
        return when {
            nombreEquipo.contains("Pontevedra", ignoreCase = true) -> R.drawable.escudo_pontevedra
            nombreEquipo.contains("Bergantiños", ignoreCase = true) -> R.drawable.escudo_bergantinos
            nombreEquipo.contains("Langreo", ignoreCase = true) -> R.drawable.escudo_langreo
            nombreEquipo.contains("Coruxo", ignoreCase = true) -> R.drawable.escudo_coruxo
            nombreEquipo.contains("Deportivo Fabril", ignoreCase = true) -> R.drawable.escudo_deportivo_fabril
            nombreEquipo.contains("Gimnástica de Torrelavega", ignoreCase = true) -> R.drawable.escudo_gimnastica
            nombreEquipo.contains("Real Avilés", ignoreCase = true) -> R.drawable.escudo_real_aviles
            nombreEquipo.contains("Marino", ignoreCase = true) -> R.drawable.escudo_marino
            nombreEquipo.contains("Numancia", ignoreCase = true) -> R.drawable.escudo_numancia
            nombreEquipo.contains("Rayo Cantabria", ignoreCase = true) -> R.drawable.escudo_rayo_cantabria
            nombreEquipo.contains("Real Ávila", ignoreCase = true) -> R.drawable.escudo_real_avila
            nombreEquipo.contains("Escobedo", ignoreCase = true) -> R.drawable.escudo_escobedo
            nombreEquipo.contains("Valladolid Promesas", ignoreCase = true) -> R.drawable.escudo_valladolid_promesas
            nombreEquipo.contains("Salamanca", ignoreCase = true) -> R.drawable.escudo_salamanca
            nombreEquipo.contains("Laredo", ignoreCase = true) -> R.drawable.escudo_laredo
            nombreEquipo.contains("Compostela", ignoreCase = true) -> R.drawable.escudo_compostela
            nombreEquipo.contains("Guijuelo", ignoreCase = true) -> R.drawable.escudo_guijuelo
            nombreEquipo.contains("Llanera", ignoreCase = true) -> R.drawable.escudo_llanera
            else -> R.drawable.team // Icono por defecto
        }
    }
}
