package com.example.fianzas.Administrador

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarEmpresaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrarEmpresa : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarEmpresaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var nombreEmpresaInput = ""
    private var nombreDuenoInput = ""
    private var emailContactoInput = ""
    private var telefonoInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnGuardarEmpresa.setOnClickListener {
            ValidarDatos()
        }

    }



    private fun ValidarDatos() {
        nombreEmpresaInput = binding.EtNombreEmpresa.text.toString().trim()
        nombreDuenoInput = binding.EtNombreDueno.text.toString().trim()
        emailContactoInput = binding.EtCorreoEmpresa.text.toString().trim()
        telefonoInput = binding.EtTelefono.text.toString().trim()
        if (nombreEmpresaInput.isEmpty()) {
            binding.EtNombreEmpresa.error = "Ingrese el nombre de la empresa"
            binding.EtNombreEmpresa.requestFocus()
        }
        else if (nombreDuenoInput.isEmpty()) {
            binding.EtNombreDueno.error = "Ingrese el nombre del dueño"
            binding.EtNombreDueno.requestFocus()
        }
        else if (emailContactoInput.isEmpty()) {
            binding.EtCorreoEmpresa.error = "Ingrese el correo"
            binding.EtCorreoEmpresa.requestFocus()
        }
        else if (telefonoInput.isEmpty()) {
            binding.EtTelefono.error = "Ingrese el teléfono"
            binding.EtTelefono.requestFocus()
        }
        else {
            CrearEmpresa()
        }

    }

    private fun CrearEmpresa() {
        progressDialog.setMessage("Creando empresa")
        progressDialog.show()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Usuario no autenticado. No se puede crear empresa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val creadorUid = currentUser.uid

        // 1. Generar un ID único para la nueva empresa
        val empresaRef = FirebaseDatabase.getInstance().getReference("Empresa")
        val empresaId = empresaRef.push().key

        if (empresaId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Error al generar ID para la empresa", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaRegistroMillis = System.currentTimeMillis()

        // 2. Preparar los datos de la empresa en un HashMap
        val datos_empresa = HashMap<String, Any>()
        datos_empresa["empresaId"] = empresaId
        datos_empresa["nombreEmpresa"] = nombreEmpresaInput
        datos_empresa["nombreDueno"] = nombreDuenoInput
        datos_empresa["emailContacto"] = emailContactoInput
        datos_empresa["telefono"] = telefonoInput
        datos_empresa["fechaRegistro"] = fechaRegistroMillis
        datos_empresa["imagen"] = ""
        datos_empresa["creadoPorUid"] = creadorUid

        // 3. Guardar los datos en Firebase Realtime Database
        empresaRef.child(empresaId).setValue(datos_empresa)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Empresa registrada exitosamente!", Toast.LENGTH_SHORT).show()
                binding.EtNombreEmpresa.text.clear()
                binding.EtNombreDueno.text.clear()
                binding.EtCorreoEmpresa.text.clear()
                binding.EtTelefono.text.clear()
                // ...
                startActivity(Intent(this@RegistrarEmpresa, MenuAdministrador::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Falló el registro de la empresa: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}