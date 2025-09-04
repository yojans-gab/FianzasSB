package com.example.fianzas.Administrador

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityEditarUsuarioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.io.path.exists

class EditarUsuarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarUsuarioBinding
    private lateinit var progressDialog: ProgressDialog

    private var usuarioUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // Obtener el UID del usuario del Intent
        usuarioUid = intent.getStringExtra("USER_UID")

        if (usuarioUid == null) {
            Toast.makeText(this, "Error: UID de usuario no encontrado", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay UID
            return
        }

        configurarRolesSpinner()
        cargarDatosUsuario()

        binding.btnGuardarCambios.setOnClickListener {
            validarYGuardarDatos()
        }
    }

    private fun configurarRolesSpinner() {
        val roles = resources.getStringArray(R.array.roles) // Define esto en strings.xml
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRolUsuarioEditar.setAdapter(adapter)
    }


    private fun cargarDatosUsuario() {
        progressDialog.setMessage("Cargando datos del usuario...")
        progressDialog.show()

        val ref =
            FirebaseDatabase.getInstance().getReference("Usuario").child(usuarioUid!!)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val rol = snapshot.child("rol").getValue(String::class.java)

                    binding.etNombreUsuarioEditar.setText(nombre)
                    binding.actvRolUsuarioEditar.setText(rol, false) // El 'false' evita que se filtre la lista al setear
                } else {
                    Toast.makeText(this@EditarUsuarioActivity, "No se encontraron datos para este usuario.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EditarUsuarioActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validarYGuardarDatos() {
        val nuevoNombre = binding.etNombreUsuarioEditar.text.toString().trim()
        val nuevoRol = binding.actvRolUsuarioEditar.text.toString().trim()

        if (nuevoNombre.isEmpty()) {
            binding.etNombreUsuarioEditar.error = "El nombre no puede estar vacío"
            binding.etNombreUsuarioEditar.requestFocus()
            return
        }
        if (nuevoRol.isEmpty()) {
            // O podrías tener una validación más específica para el rol
            Toast.makeText(this, "Debe seleccionar un rol", Toast.LENGTH_SHORT).show()
            binding.actvRolUsuarioEditar.requestFocus()
            return
        }

        actualizarDatosEnFirebase(nuevoNombre, nuevoRol)
    }

    private fun actualizarDatosEnFirebase(nombre: String, rol: String) {
        progressDialog.setMessage("Actualizando datos...")
        progressDialog.show()

        val updatesMap = HashMap<String, Any>()
        updatesMap["nombre"] = nombre
        updatesMap["rol"] = rol


        val ref =
            FirebaseDatabase.getInstance().getReference("Usuario").child(usuarioUid!!)
        ref.updateChildren(updatesMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad de edición
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}