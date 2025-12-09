package com.example.fianzas.Administrador

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityEditarEmpresaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class EditarEmpresa : AppCompatActivity() {

    private lateinit var binding: ActivityEditarEmpresaBinding
    private lateinit var progressDialog: ProgressDialog

    private var empresaId: String = ""
    private var imageUri: Uri? = null // URI de la NUEVA imagen seleccionada
    private var urlImagenActual: String = "" // URL que ya existe en Firebase

    // Launcher para seleccionar imagen de la galería
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivIconoEmpresa.setImageURI(uri) // Previsualizar la nueva imagen local
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEmpresaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this).apply {
            setTitle("Por favor espere")
            setCanceledOnTouchOutside(false)
        }

        empresaId = (intent.getStringExtra("EMPRESA_ID") ?: "").trim()
        if (empresaId.isEmpty()) {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 1. Cargar datos existentes
        cargarDatosEmpresa()

        // 2. Configurar click en la imagen para cambiarla
        binding.ivIconoEmpresa.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // 3. Botón Guardar
        binding.BtnEditarEmpresa.setOnClickListener {
            validarDatos()
        }
    }

    private fun cargarDatosEmpresa() {
        progressDialog.setMessage("Cargando datos...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Empresa").child(empresaId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (!snapshot.exists()) {
                    finish()
                    return
                }

                // Cargar Textos
                val nombre = snapshot.child("nombreEmpresa").getValue(String::class.java) ?: ""
                val dueno = snapshot.child("nombreDueno").getValue(String::class.java) ?: ""
                val correo = snapshot.child("emailContacto").getValue(String::class.java) ?: "" // Ojo con la clave, en Registrar usaste emailContacto
                val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""

                // Cargar URL de imagen
                urlImagenActual = snapshot.child("urlLogoEmpresa").getValue(String::class.java) ?: ""

                binding.EtNombreEditarEmpresa.setText(nombre)
                binding.EtNombreEditarDueno.setText(dueno)
                binding.EtCorreoEditarEmpresa.setText(correo)
                binding.EtEditarTelefono.setText(telefono)

                // Mostrar imagen actual con Glide
                try {
                    Glide.with(this@EditarEmpresa)
                        .load(urlImagenActual)
                        .placeholder(R.drawable.ic_empresa) // Tu icono por defecto
                        .error(R.drawable.ic_empresa)
                        .into(binding.ivIconoEmpresa)
                } catch (e: Exception) { }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun validarDatos() {
        val nuevoNombre = binding.EtNombreEditarEmpresa.text.toString().trim()
        val nuevoDueno = binding.EtNombreEditarDueno.text.toString().trim()
        val nuevoCorreo = binding.EtCorreoEditarEmpresa.text.toString().trim()
        val nuevoTelefono = binding.EtEditarTelefono.text.toString().trim()

        if (nuevoNombre.isEmpty()) { binding.EtNombreEditarEmpresa.error = "Requerido"; return }
        if (nuevoDueno.isEmpty()) { binding.EtNombreEditarDueno.error = "Requerido"; return }
        if (!Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) { binding.EtCorreoEditarEmpresa.error = "Correo inválido"; return }
        if (nuevoTelefono.isEmpty()) { binding.EtEditarTelefono.error = "Requerido"; return }

        // DECISIÓN: ¿Subimos imagen nueva o solo texto?
        if (imageUri == null) {
            // Caso A: Solo actualizar textos (conservar imagen vieja)
            actualizarInfoBaseDatos(nuevoNombre, nuevoDueno, nuevoCorreo, nuevoTelefono, urlImagenActual)
        } else {
            // Caso B: Subir NUEVA imagen
            subirNuevaImagen(nuevoNombre, nuevoDueno, nuevoCorreo, nuevoTelefono)
        }
    }

    private fun subirNuevaImagen(nombre: String, dueno: String, correo: String, telefono: String) {
        progressDialog.setMessage("Actualizando imagen...")
        progressDialog.show()

        val nombreImagen = "Empresas_Logos/$empresaId"
        val storageRef = FirebaseStorage.getInstance().getReference(nombreImagen)

        // Subir archivo (sobrescribirá el existente con el mismo nombre)
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val nuevaUrl = uri.toString()
                    // Una vez tenemos la URL nueva, actualizamos la BD
                    actualizarInfoBaseDatos(nombre, dueno, correo, telefono, nuevaUrl)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarInfoBaseDatos(nombre: String, dueno: String, correo: String, telefono: String, urlImagen: String) {
        progressDialog.setMessage("Guardando cambios...")
        if(!progressDialog.isShowing) progressDialog.show()

        val updates = hashMapOf<String, Any>(
            "nombreEmpresa" to nombre,
            "nombreDueno" to dueno,
            "emailContacto" to correo, // Asegúrate de usar la misma clave que en Registrar
            "telefono" to telefono,
            "urlLogoEmpresa" to urlImagen
        )

        FirebaseDatabase.getInstance().getReference("Empresa")
            .child(empresaId)
            .updateChildren(updates)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "¡Empresa actualizada!", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad y vuelve a la lista
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error BD: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}