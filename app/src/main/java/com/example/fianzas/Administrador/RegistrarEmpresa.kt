package com.example.fianzas.Administrador

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarEmpresaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class RegistrarEmpresa : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarEmpresaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // Variables para datos
    private var imageUri: Uri? = null // Para guardar la URI de la imagen seleccionada
    private var nombreEmpresaInput = ""
    private var nombreDuenoInput = ""
    private var emailContactoInput = ""
    private var telefonoInput = ""

    // Contrato para abrir galería
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            // Mostramos la imagen seleccionada en el ImageView usando Glide
            Glide.with(this).load(imageUri).into(binding.IvLogoEmpresa)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // Evento al tocar la imagen para seleccionarla
        binding.IvLogoEmpresa.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

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
        } else if (nombreDuenoInput.isEmpty()) {
            binding.EtNombreDueno.error = "Ingrese el nombre del dueño"
        } else if (emailContactoInput.isEmpty()) {
            binding.EtCorreoEmpresa.error = "Ingrese el correo"
        } else if (telefonoInput.isEmpty()) {
            binding.EtTelefono.error = "Ingrese el teléfono"
        } else {
            // Si todo está bien, procedemos a guardar
            GuardarDatos()
        }
    }

    private fun GuardarDatos() {
        progressDialog.setMessage("Guardando información...")
        progressDialog.show()

        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Generamos el ID primero, lo necesitamos para el nombre de la imagen
        val empresaRef = FirebaseDatabase.getInstance().getReference("Empresa")
        val empresaId = empresaRef.push().key ?: ""

        if (imageUri == null) {
            // CASO 1: Guardar SIN imagen
            CrearEmpresaEnBD(empresaId, "")
        } else {
            // CASO 2: Guardar CON imagen
            // Estructura en Storage: Empresas_Logos/empresaId
            val filePathAndName = "Empresas_Logos/$empresaId"
            val storageRef = FirebaseStorage.getInstance().getReference(filePathAndName)

            storageRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Imagen subida, ahora obtenemos la URL
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri = uriTask.result

                    if (uriTask.isSuccessful) {
                        // Ya tenemos la URL, guardamos en base de datos
                        CrearEmpresaEnBD(empresaId, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun CrearEmpresaEnBD(empresaId: String, urlImagen: String) {
        val currentUser = firebaseAuth.currentUser
        val creadorUid = currentUser?.uid ?: ""
        val fechaRegistroMillis = System.currentTimeMillis()

        val datos_empresa = HashMap<String, Any>()
        datos_empresa["empresaId"] = empresaId
        datos_empresa["nombreEmpresa"] = nombreEmpresaInput
        datos_empresa["nombreDueno"] = nombreDuenoInput
        datos_empresa["emailContacto"] = emailContactoInput
        datos_empresa["telefono"] = telefonoInput
        datos_empresa["fechaRegistro"] = fechaRegistroMillis
        // IMPORTANTE: Usamos la misma clave que en tu Modelo ("urlLogoEmpresa")
        datos_empresa["urlLogoEmpresa"] = urlImagen
        datos_empresa["creadoPorUid"] = creadorUid

        val empresaRef = FirebaseDatabase.getInstance().getReference("Empresa")
        empresaRef.child(empresaId).setValue(datos_empresa)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Empresa registrada exitosamente!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegistrarEmpresa, MenuAdministrador::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error en BD: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}