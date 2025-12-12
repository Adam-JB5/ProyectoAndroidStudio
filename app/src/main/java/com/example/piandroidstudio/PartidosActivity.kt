package com.example.piandroidstudio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

data class Partido(
    val equipoLocal: String,
    val equipoVisitante: String,
    val hora: String
)

class PartidosActivity : AppCompatActivity() {

    private val equiposSeguidos = listOf("Real Madrid", "Barcelona") // Ejemplo
    private val todosPartidos = listOf(
        Partido("Real Madrid", "Barcelona", "21:00"),
        Partido("Atl. Madrid", "Sevilla", "19:00"),
        Partido("Valencia", "Real Sociedad", "22:00")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        setupBottomNav(R.id.bottomNav)

        val rvEquiposSeguidos: RecyclerView = findViewById(R.id.rvEquiposSeguidos)
        val rvTodosPartidos: RecyclerView = findViewById(R.id.rvTodosPartidos)

        // Filtrar partidos de los equipos seguidos
        val partidosSeguidos = todosPartidos.filter {
            equiposSeguidos.contains(it.equipoLocal) || equiposSeguidos.contains(it.equipoVisitante)
        }

        // Configurar RecyclerViews
        rvEquiposSeguidos.layoutManager = LinearLayoutManager(this)
        rvEquiposSeguidos.adapter = PartidoAdapter(partidosSeguidos)

        rvTodosPartidos.layoutManager = LinearLayoutManager(this)
        rvTodosPartidos.adapter = PartidoAdapter(todosPartidos)
    }
}

// Adapter único para ambos RecyclerViews
class PartidoAdapter(private val partidos: List<Partido>) : RecyclerView.Adapter<PartidoAdapter.PartidoViewHolder>() {

    class PartidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtLocal: TextView = itemView.findViewById(R.id.txtEquipoLocal)
        val txtVisitante: TextView = itemView.findViewById(R.id.txtEquipoVisitante)
        val txtHora: TextView = itemView.findViewById(R.id.txtHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_partido, parent, false)
        return PartidoViewHolder(view)
    }

    override fun getItemCount(): Int = partidos.size

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        val partido = partidos[position]
        holder.txtLocal.text = partido.equipoLocal
        holder.txtVisitante.text = partido.equipoVisitante
        holder.txtHora.text = partido.hora
    }
}
