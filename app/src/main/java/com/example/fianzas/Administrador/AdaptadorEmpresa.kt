package com.example.fianzas.Administrador

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fianzas.R // Asegúrate de importar tu R
import com.example.fianzas.databinding.ItemEmpresasBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AdaptadorEmpresa(
    private val mContext: Context,
    private val empresaArrayList: ArrayList<ModeloEmpresas>
) : RecyclerView.Adapter<AdaptadorEmpresa.HolderEmpresa>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderEmpresa {
        val binding = ItemEmpresasBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return HolderEmpresa(binding)
    }

    override fun onBindViewHolder(holder: HolderEmpresa, position: Int) {
        val modelo = empresaArrayList[position]

        // Asignación de textos
        holder.binding.tvNombreEmpresa.text = modelo.nombreEmpresa
        holder.binding.tvNombreDueno.text = modelo.nombreDueno

        // --- NUEVO: Cargar imagen con Glide ---
        try {
            Glide.with(mContext)
                .load(modelo.urlLogoEmpresa) // Carga la URL
                .placeholder(R.drawable.ic_launcher_background) // Imagen mientras carga
                .error(R.drawable.ic_launcher_background) // Imagen si falla o está vacía
                .into(holder.binding.ivFotoEmpresa) // Asegúrate que este ID exista en item_empresas.xml
        } catch (e: Exception) {
            Log.e("AdaptadorEmpresa", "Error al cargar imagen: ${e.message}")
        }

        // Click editar
        holder.binding.btnEditarEmpresa.setOnClickListener {
            val id = modelo.empresaId.trim()
            if (id.isEmpty()) {
                Toast.makeText(mContext, "ID de empresa no encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(mContext, EditarEmpresa::class.java)
            i.putExtra("EMPRESA_ID", id)
            mContext.startActivity(i)
        }

        // Click eliminar
        holder.binding.btnEliminarEmpresa.setOnClickListener {
            confirmarEliminacionEmpresa(modelo)
        }
    }

    override fun getItemCount(): Int = empresaArrayList.size

    private fun confirmarEliminacionEmpresa(modeloEmpresa: ModeloEmpresas) {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Eliminar Empresa")
        builder.setMessage("¿Estás seguro de que deseas eliminar la empresa '${modeloEmpresa.nombreEmpresa}'?\nEsto eliminará sus datos y su logo.")
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            eliminarEmpresaTotalmente(modeloEmpresa) // Cambié el nombre de la función para reflejar que borra todo
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // --- NUEVO: Función mejorada para borrar Storage + Database ---
    private fun eliminarEmpresaTotalmente(modeloEmpresa: ModeloEmpresas) {
        val idEmpresa = modeloEmpresa.empresaId
        val urlImagen = modeloEmpresa.urlLogoEmpresa

        if (idEmpresa.isEmpty()) {
            Toast.makeText(mContext, "ID de empresa no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Paso 1: Intentar borrar la imagen si existe
        if (urlImagen.isNotEmpty()) {
            try {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(urlImagen)
                storageRef.delete()
                    .addOnSuccessListener {
                        Log.d("AdaptadorEmpresa", "Imagen eliminada de Storage")
                        // Una vez borrada la imagen, borramos el registro de la BD
                        borrarRegistroBaseDatos(idEmpresa, modeloEmpresa.nombreEmpresa)
                    }
                    .addOnFailureListener { e ->
                        // Si falla borrar la imagen (ej. ya no existe), igual intentamos borrar el registro
                        Log.e("AdaptadorEmpresa", "Error al borrar imagen: ${e.message}")
                        borrarRegistroBaseDatos(idEmpresa, modeloEmpresa.nombreEmpresa)
                    }
            } catch (e: Exception) {
                // Si la URL es inválida o hay otro error, procedemos a borrar el registro
                borrarRegistroBaseDatos(idEmpresa, modeloEmpresa.nombreEmpresa)
            }
        } else {
            // Si no tiene imagen, borramos directo de la BD
            borrarRegistroBaseDatos(idEmpresa, modeloEmpresa.nombreEmpresa)
        }
    }

    // Función auxiliar para no repetir código
    private fun borrarRegistroBaseDatos(idEmpresa: String, nombreEmpresa: String) {
        FirebaseDatabase.getInstance().getReference("Empresa")
            .child(idEmpresa)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Empresa '$nombreEmpresa' eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al eliminar empresa de BD: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderEmpresa(val binding: ItemEmpresasBinding) : RecyclerView.ViewHolder(binding.root)
}