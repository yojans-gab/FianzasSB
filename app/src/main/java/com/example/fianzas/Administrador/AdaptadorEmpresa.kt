package com.example.fianzas.Administrador

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.Gestor.ModeloEmpresas
import com.example.fianzas.databinding.ItemEmpresasBinding
import com.google.firebase.database.FirebaseDatabase

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
        val empresaId = modelo.empresaId
        val nombreEmpresa = modelo.nombreEmpresa
        val nombreDueno = modelo.nombreDueno


        holder.binding.tvNombreEmpresa.text = nombreEmpresa
        holder.binding.tvNombreDueno.text = nombreDueno

        // Click editar (ejemplo)
        holder.binding.btnEditarEmpresa.setOnClickListener {
            Toast.makeText(mContext, "Editar: $nombreEmpresa", Toast.LENGTH_SHORT).show()
            // lanzar Activity editar si lo tienes
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
        builder.setMessage("¿Estás seguro de que deseas eliminar la empresa '${modeloEmpresa.nombreEmpresa}'?\nEsto también podría afectar a las fianzas asociadas.")
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            eliminarEmpresaDeFirebase(modeloEmpresa)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun eliminarEmpresaDeFirebase(modeloEmpresa: ModeloEmpresas) {
        val idEmpresa = modeloEmpresa.empresaId
        if (idEmpresa.isEmpty()) {
            Toast.makeText(mContext, "ID de empresa no válido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Usa el mismo nombre de nodo que usas para listar (aquí 'Empresas')
        val refEmpresa = FirebaseDatabase.getInstance().getReference("Empresas").child(idEmpresa)
        refEmpresa.removeValue()
            .addOnSuccessListener {
                Toast.makeText(mContext, "Empresa '${modeloEmpresa.nombreEmpresa}' eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(mContext, "Error al eliminar empresa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderEmpresa(val binding: ItemEmpresasBinding) : RecyclerView.ViewHolder(binding.root)
}
