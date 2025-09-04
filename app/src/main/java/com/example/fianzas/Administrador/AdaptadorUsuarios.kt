package com.example.fianzas.Administrador

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.databinding.ItemUsuariosBinding // Asegúrate que este es el nombre correcto de tu layout de item
import com.google.firebase.database.FirebaseDatabase

class AdaptadorUsuarios(
    private val m_context: Context,
    private val usuariosArrayList: ArrayList<ModeloUsuarios>
) : RecyclerView.Adapter<AdaptadorUsuarios.HolderUsuario>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {

        val binding = ItemUsuariosBinding.inflate(LayoutInflater.from(m_context), parent, false)
        return HolderUsuario(binding)
    }

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val modeloUsuarios = usuariosArrayList[position]

        // Acceder a las vistas a través de holder.binding
        holder.binding.tvNombreUsuario.text = modeloUsuarios.nombre
        holder.binding.tvCorreoUsuario.text = modeloUsuarios.email
        holder.binding.tvRolUsuario.text = modeloUsuarios.rol

        holder.binding.btnEditarUsuario.setOnClickListener {
            val intent = Intent(m_context, EditarUsuarioActivity::class.java)
            // Pasamos el UID del usuario a la actividad de edición
            intent.putExtra("USER_UID", modeloUsuarios.uid)
            m_context.startActivity(intent)
        }
        holder.binding.btnEliminarUsuario.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(m_context)
            builder.setTitle("Eliminar Usuario")
            builder.setMessage("¿Estás seguro de que deseas eliminar este usuario?")
            builder.setPositiveButton("Eliminar") { dialog, which ->
                EliminarUsuario(modeloUsuarios, holder)
            }
            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun EliminarUsuario(modeloUsuarios: ModeloUsuarios, holder: HolderUsuario) {
        val id = modeloUsuarios.uid
        val ref = FirebaseDatabase.getInstance().getReference("Usuario")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(m_context, "Usuario eliminado...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(m_context, "No se pudo eliminar el usuario debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getItemCount(): Int {
        return usuariosArrayList.size
    }


    inner class HolderUsuario(val binding: ItemUsuariosBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}

