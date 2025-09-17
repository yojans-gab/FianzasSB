package com.example.fianzas.Gestor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.Administrador.EditarEmpresa
import com.example.fianzas.Administrador.EditarUsuarioActivity
import com.example.fianzas.databinding.ItemFianzasBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorFianza(
    private val mContext: Context,
    private val fianzasList: ArrayList<ModeloFianza>
) : RecyclerView.Adapter<AdaptadorFianza.HolderFianza>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFianza {
        val binding = ItemFianzasBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderFianza(binding)
    }

    override fun onBindViewHolder(holder: HolderFianza, position: Int) {
        val modelo = fianzasList[position]

        // Texto
        holder.binding.tvTipoFianzaItem.text = modelo.tipoFianza.ifEmpty { "Sin tipo" }
        holder.binding.tvBeneficiarioItem.text = modelo.beneficiario.ifEmpty { "Sin beneficiario" }

        // Fechas: manejo si vienen en segundos o milisegundos
        holder.binding.tvFechaEmisionItem.text = "Emisión: ${formatDate(modelo.fechaEmision)}"
        holder.binding.tvFechaVencimientoItem.text = "Vence: ${formatDate(modelo.fechaVencimiento)}"

        // Menú de opciones (editar / eliminar)
        holder.binding.btnMenuFianza.setOnClickListener { view ->
            val popup = PopupMenu(mContext, view)
            popup.menu.add(Menu.NONE, 1, 1, "Editar")
            popup.menu.add(Menu.NONE, 2, 2, "Eliminar")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val intent = Intent(mContext, EditarFianza::class.java)
                        intent.putExtra("Fianza_Id", modelo.fianzaId)
                        mContext.startActivity(intent)
                        true
                    }
                    2 -> {
                        if (modelo.fianzaId.isNullOrEmpty()) {
                            Toast.makeText(mContext, "ID inválido.", Toast.LENGTH_SHORT).show()
                            true
                        } else {
                            eliminarFianza(modelo.fianzaId, modelo.nog)
                            true
                        }
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = fianzasList.size

    private fun eliminarFianza(fianzaId: String?, nog: String) {
        if (fianzaId.isNullOrEmpty()) {
            Toast.makeText(mContext, "ID de fianza no válido.", Toast.LENGTH_SHORT).show()
            return
        }
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)
        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Fianza $nog eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDate(epoch: Long): String {
        if (epoch <= 0L) return "N/A"
        // Si viene en segundos en lugar de ms, detectar y convertir:
        val ms = if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(ms))
        } catch (e: Exception) {
            "N/A"
        }
    }

    inner class HolderFianza(val binding: ItemFianzasBinding) : RecyclerView.ViewHolder(binding.root)
}
