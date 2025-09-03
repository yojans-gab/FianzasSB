package com.example.fianzas.Administrador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fianzas.databinding.ItemUsuariosBinding

class AdaptadorUsuarios : RecyclerView.Adapter<AdaptadorUsuarios.HolderUsuario> {

    private lateinit var binding: ItemUsuariosBinding

    private val m_context: Context
    private val usuariosArrayList: ArrayList<ModeloUsuarios>

    constructor(m_context: Context, usuariosArrayList: ArrayList<ModeloUsuarios>) {
        this.m_context = m_context
        this.usuariosArrayList = usuariosArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUsuario {
        binding = ItemUsuariosBinding.inflate(LayoutInflater.from(m_context), parent, false)
        return HolderUsuario(binding.root)
    }

    override fun onBindViewHolder(holder: HolderUsuario, position: Int) {
        val modeloUsuarios = usuariosArrayList[position]
        val email = modeloUsuarios.email
        val imagen = modeloUsuarios.imagen
        val nombre = modeloUsuarios.nombre
        val rol = modeloUsuarios.rol
        val password = modeloUsuarios.password
        val tiempo = modeloUsuarios.tiempo
        val uid = modeloUsuarios.uid

        holder.usuario.text = nombre
        holder.correo.text = email
        holder.rol.text = rol
    }

    override fun getItemCount(): Int {
        return usuariosArrayList.size
    }


    inner class HolderUsuario (itemView: View): RecyclerView.ViewHolder(itemView){
        var usuario = binding.tvNombreUsuario
        var correo = binding.tvCorreoUsuario
        var rol = binding.tvRolUsuario
        var imagen = binding.ivFotoUsuario
        var editar = binding.btnEditarUsuario
        var eliminar = binding.btnEliminarUsuario
    }
}