package com.example.fianzas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.databinding.ItemFianzaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FianzaAdapter(
    private val fianzasList: ArrayList<Fianza>,
    private val onItemClick: (Fianza) -> Unit // Lambda para manejar clics
) : RecyclerView.Adapter<FianzaAdapter.FianzaViewHolder>() {

    inner class FianzaViewHolder(val binding: ItemFianzaBinding) : RecyclerView.ViewHolder(binding.root) {
        // Bloque init para configurar el listener del clic
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(fianzasList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FianzaViewHolder {
        val binding = ItemFianzaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FianzaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FianzaViewHolder, position: Int) {
        val fianza = fianzasList[position]
        val b = holder.binding

        // --- CORRECCIÓN DE ERRORES ---

        // 1. Mostrar Tipo de Fianza (tu código estaba bien aquí)
        b.tvTipoFianzaItem.text = fianza.tipoFianza?.takeIf { it.isNotEmpty() } ?: "Sin tipo"

        // 2. CORREGIDO: Usar 'nombreProyecto' que sí existe en la clase Fianza
        b.tvNombreProyectoItem.text = fianza.nombreProyecto?.takeIf { it.isNotEmpty() } ?: "Sin proyecto"

        // 3. CORREGIDO: Mostrar la fecha de vencimiento correctamente.
        // Si quieres mostrar ambas fechas, necesitas otro TextView en tu layout.
        // Por ahora, mostramos la más importante: Vencimiento.
        b.tvFechaVencimientoItem.text = "Vence: ${formatDate(fianza.fechaVencimiento)}"
    }

    override fun getItemCount(): Int = fianzasList.size

    private fun formatDate(epoch: Long): String {
        if (epoch <= 0L) return "N/A"
        val ms = if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(ms))
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }
}
