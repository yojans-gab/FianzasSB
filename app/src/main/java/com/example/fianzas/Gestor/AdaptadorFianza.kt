package com.example.fianzas.Gestor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fianzas.R
import com.example.fianzas.databinding.ItemFianzasBinding
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorFianza(
    private val mContext: Context,
    private val fianzasList: ArrayList<ModeloFianza>,
    // Recibimos el mapa: Clave=ID_Empresa, Valor=URL_Logo
    private val logosMap: HashMap<String, String>
) : RecyclerView.Adapter<AdaptadorFianza.HolderFianza>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderFianza {
        // Inflamos el diseño del item (asegúrate de que item_fianzas.xml sea el nombre correcto)
        val binding = ItemFianzasBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderFianza(binding)
    }

    override fun onBindViewHolder(holder: HolderFianza, position: Int) {
        // Obtenemos el modelo de la fianza actual
        val modelo = fianzasList[position]

        // 1. Asignación de Textos
        holder.binding.tvTipoFianzaItem.text = modelo.tipoFianza.ifEmpty { "Sin tipo" }
        holder.binding.tvBeneficiarioItem.text = modelo.beneficiario.ifEmpty { "Sin beneficiario" }
        holder.binding.tvFechaEmisionItem.text = "Emisión: ${formatDate(modelo.fechaEmision)}"
        holder.binding.tvFechaVencimientoItem.text = "Vence: ${formatDate(modelo.fechaVencimiento)}"

        // 2. Carga del Logo de la Empresa usando el Mapa
        val idEmpresa = modelo.empresaId
        val urlLogo = logosMap[idEmpresa] // Buscamos la URL en el diccionario

        try {
            Glide.with(mContext)
                .load(urlLogo) // Si es null, Glide usa el placeholder
                .placeholder(R.drawable.ic_empresa) // Icono mientras carga
                .error(R.drawable.ic_empresa)       // Icono si falla o no hay imagen
                .centerCrop()
                .into(holder.binding.ivLogoEmpresaFianza) // ID del ImageView en tu XML
        } catch (e: Exception) {
            holder.binding.ivLogoEmpresaFianza.setImageResource(R.drawable.ic_empresa)
        }

        // 3. Evento Click en TODA la tarjeta para ver DETALLES
        holder.itemView.setOnClickListener {
            // Validamos que el ID no esté vacío
            if (modelo.fianzaId.isNotEmpty()) {
                val intent = Intent(mContext, DetalleFianza::class.java)
                intent.putExtra("FIANZA_ID", modelo.fianzaId) // Enviamos el ID a la otra actividad
                mContext.startActivity(intent)
            } else {
                Toast.makeText(mContext, "Error: ID de fianza no válido", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Configuración del Menú de 3 puntos (Editar / Eliminar)
        holder.binding.btnMenuFianza.setOnClickListener { view ->
            val popup = PopupMenu(mContext, view)
            popup.menu.add(Menu.NONE, 1, 1, "Editar")
            popup.menu.add(Menu.NONE, 2, 2, "Eliminar")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        // Opción Editar
                        val intent = Intent(mContext, EditarFianza::class.java)
                        intent.putExtra("Fianza_Id", modelo.fianzaId)
                        mContext.startActivity(intent)
                        true
                    }
                    2 -> {
                        // Opción Eliminar
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

    override fun getItemCount(): Int {
        return fianzasList.size
    }

    // Función auxiliar para eliminar de Firebase
    private fun eliminarFianza(fianzaId: String, nog: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)
        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Fianza $nog eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función auxiliar para formatear la fecha (Epoch -> String)
    private fun formatDate(epoch: Long): String {
        if (epoch <= 0L) return "N/A"
        // Aseguramos que esté en milisegundos
        val ms = if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(ms))
        } catch (e: Exception) {
            "N/A"
        }
    }

    // ViewHolder
    inner class HolderFianza(val binding: ItemFianzasBinding) : RecyclerView.ViewHolder(binding.root)
}