package com.example.fianzas.Gestor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Asegúrate de tener esta importación
import com.example.fianzas.R
import com.example.fianzas.databinding.ItemFianzasBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorFianza(
    private val mContext: Context,
    private val fianzasList: ArrayList<ModeloFianza>,
    // NUEVO: Recibimos el diccionario de logos
    private val logosMap: HashMap<String, String>
) : RecyclerView.Adapter<AdaptadorFianza.HolderFianza>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFianza {
        val binding = ItemFianzasBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderFianza(binding)
    }

    override fun onBindViewHolder(holder: HolderFianza, position: Int) {
        val modelo = fianzasList[position]

        // Textos
        holder.binding.tvTipoFianzaItem.text = modelo.tipoFianza.ifEmpty { "Sin tipo" }
        holder.binding.tvBeneficiarioItem.text = modelo.beneficiario.ifEmpty { "Sin beneficiario" }
        holder.binding.tvFechaEmisionItem.text = "Emisión: ${formatDate(modelo.fechaEmision)}"
        holder.binding.tvFechaVencimientoItem.text = "Vence: ${formatDate(modelo.fechaVencimiento)}"

        // --- CARGAR LOGO DE EMPRESA ---
        val idEmpresa = modelo.empresaId
        // Buscamos en el mapa si tenemos la URL para este ID
        val urlLogo = logosMap[idEmpresa]

        try {
            Glide.with(mContext)
                .load(urlLogo) // Si urlLogo es null, Glide usa el placeholder
                .placeholder(R.drawable.ic_empresa) // Pon aquí tu icono por defecto
                .error(R.drawable.ic_empresa)       // Si falla la carga
                .centerCrop() // Importante para que se vea bien en tu ShapeableImageView
                .into(holder.binding.ivLogoEmpresaFianza) // Tu ID del XML
        } catch (e: Exception) {
            holder.binding.ivLogoEmpresaFianza.setImageResource(R.drawable.ic_empresa)
        }
        // ------------------------------

        // Menú (Código existente sin cambios)
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
                        if (modelo.fianzaId.isNotEmpty()) {
                            eliminarFianza(modelo.fianzaId, modelo.nog)
                        } else {
                            Toast.makeText(mContext, "ID inválido.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = fianzasList.size

    // ... tus funciones eliminarFianza y formatDate siguen igual ...
    private fun eliminarFianza(fianzaId: String, nog: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)
        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Fianza $nog eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDate(epoch: Long): String {
        if (epoch <= 0L) return "N/A"
        val ms = if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(ms))
        } catch (e: Exception) { "N/A" }
    }

    inner class HolderFianza(val binding: ItemFianzasBinding) : RecyclerView.ViewHolder(binding.root)
}