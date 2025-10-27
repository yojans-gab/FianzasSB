package com.example.fianzas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.databinding.ItemFianzaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FianzaAdapter(
    private val fianzasList: ArrayList<Fianza>
) : RecyclerView.Adapter<FianzaAdapter.FianzaViewHolder>() {

    inner class FianzaViewHolder(val binding: ItemFianzaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FianzaViewHolder {
        val binding = ItemFianzaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FianzaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FianzaViewHolder, position: Int) {
        val fianza = fianzasList[position]
        val b = holder.binding

        b.tvTipoFianzaItem.text = if (fianza.tipoFianza.isNotBlank()) fianza.tipoFianza else "Sin tipo"
        b.tvNombreProyectoItem?.text = if (fianza.beneficiario.isNotBlank()) fianza.beneficiario else "Sin beneficiario"
        // Si tu item_fianza tiene tvNombreProyecto o similar, usa ese id.
        // Ejemplo (si existe):
        // b.tvNombreProyectoItem.text = if (fianza.nombreProyecto.isNotBlank()) fianza.nombreProyecto else "Sin proyecto"

        b.tvFechaVencimientoItem?.text = "Emisión: ${formatDate(fianza.fechaEmision)}"
        b.tvFechaVencimientoItem?.text = "Vence: ${formatDate(fianza.fechaVencimiento)}"
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
