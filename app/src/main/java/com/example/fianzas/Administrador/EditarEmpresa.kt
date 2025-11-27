package com.example.fianzas.Administrador

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityEditarEmpresaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.io.path.exists

class EditarEmpresa : AppCompatActivity() {
    private lateinit var binding: ActivityEditarEmpresaBinding
    private lateinit var progressDialog: ProgressDialog
    private var empresaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Por favor espere")
            setCanceledOnTouchOutside(false)
        }

        // MISMA CLAVE que envías desde el adapter
        empresaId = (intent.getStringExtra("EMPRESA_ID") ?: "").trim()
        if (empresaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de empresa no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarDatosEmpresa()

        binding.BtnEditarEmpresa.setOnClickListener { validarYGuardarDatos() }
    }

    private fun cargarDatosEmpresa() {
        progressDialog.setMessage("Cargando datos de la empresa...")
        progressDialog.show()

        // MISMO NODO: "Empresa" (singular)
        val ref = FirebaseDatabase.getInstance()
            .getReference("Empresa")
            .child(empresaId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (!snapshot.exists()) {
                    Toast.makeText(this@EditarEmpresa,
                        "No se encontraron datos para esta empresa.", Toast.LENGTH_SHORT).show()
                    return
                }

                // MISMOS CAMPOS que guardas en RTDB
                val nombre = snapshot.child("nombreEmpresa").getValue(String::class.java) ?: ""
                val dueno  = snapshot.child("nombreDueno").getValue(String::class.java) ?: ""
                val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""

                binding.EtNombreEditarEmpresa.setText(nombre)
                binding.EtNombreEditarDueno.setText(dueno)
                binding.EtCorreoEditarEmpresa.setText(correo)
                binding.EtEditarTelefono.setText(telefono)
            }
            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EditarEmpresa,
                    "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validarYGuardarDatos() {
        val nuevoNombre = binding.EtNombreEditarEmpresa.text.toString().trim()
        val nuevoDueno = binding.EtNombreEditarDueno.text.toString().trim()
        val nuevoCorreo = binding.EtCorreoEditarEmpresa.text.toString().trim()
        val nuevoTelefono = binding.EtEditarTelefono.text.toString().trim()

        if (nuevoNombre.isEmpty()) { binding.EtNombreEditarEmpresa.error = "Requerido"; return }
        if (nuevoDueno.isEmpty()) { binding.EtNombreEditarDueno.error = "Requerido"; return }
        if (!Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
            binding.EtCorreoEditarEmpresa.error = "Correo inválido"; return
        }
        if (nuevoTelefono.isEmpty()) { binding.EtEditarTelefono.error = "Requerido"; return }

        actualizarDatosEnFirebase(nuevoNombre, nuevoDueno, nuevoCorreo, nuevoTelefono)
    }

    private fun actualizarDatosEnFirebase(nombre: String, dueno: String, correo: String, telefono: String) {
        progressDialog.setMessage("Actualizando datos...")
        progressDialog.show()

        val updates = hashMapOf<String, Any>(
            "nombreEmpresa" to nombre,   // ← mismos nombres del modelo
            "nombreDueno"   to dueno,
            "correo"        to correo,
            "telefono"      to telefono
        )

        FirebaseDatabase.getInstance()
            .getReference("Empresa")     // ← mismo nodo
            .child(empresaId)
            .updateChildren(updates)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}