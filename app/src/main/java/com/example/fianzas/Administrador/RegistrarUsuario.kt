package com.example.fianzas.Administrador

import android.app.Fragment
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegistrarUsuario : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarUsuarioBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistrarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roles = resources.getStringArray(R.array.roles)
        val adapterArray = ArrayAdapter(this, R.layout.dropdown_item, roles)
        binding.autoCompleteTextView.setAdapter(adapterArray)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnRegistrarAdmin.setOnClickListener {
            ValidarInformacion()
        }

        binding.btnBack.setOnClickListener {
            OnBackPressedDispatcher().onBackPressed()
        }

    }

    var nombre = ""
    var email = ""
    var rol = ""
    var password = ""
    var rpassword = ""

    private fun ValidarInformacion() {
        nombre = binding.EtNombresAdmin.text.toString().trim()
        email = binding.EtEmailAdmin.text.toString().trim()
        rol = binding.autoCompleteTextView.text.toString().trim()
        password = binding.EtPasswordAdmin.text.toString().trim()
        rpassword = binding.EtRPasswordAdmin.text.toString().trim()

        if (nombre.isEmpty()) {
            binding.EtNombresAdmin.error = "Ingrese su nombre"
            binding.EtNombresAdmin.requestFocus()
        }
        else if (email.isEmpty()) {
            binding.EtEmailAdmin.error = "Ingrese su email"
            binding.EtEmailAdmin.requestFocus()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmailAdmin.error = "Ingrese un email valido"
            binding.EtEmailAdmin.requestFocus()
        }
        else if (rol.isEmpty()) {
            binding.autoCompleteTextView.error = "Seleccione su rol"
            binding.autoCompleteTextView.requestFocus()
        }
        else if (password.isEmpty()) {
            binding.EtPasswordAdmin.error = "Ingrese su contraseña"
            binding.EtPasswordAdmin.requestFocus()
        }
        else if (password.length < 6) {
            binding.EtPasswordAdmin.error = "La contraseña debe tener al menos 6 caracteres"
            binding.EtPasswordAdmin.requestFocus()
        }
        else if (rpassword.isEmpty()) {
            binding.EtRPasswordAdmin.error = "Repita su contraseña"
            binding.EtRPasswordAdmin.requestFocus()
        }
        else if (password != rpassword) {
            binding.EtRPasswordAdmin.error = "Las contraseñas no coinciden"
            binding.EtRPasswordAdmin.requestFocus()
        }
        else {
            CrearCuenta(email,password,rol)
        }

    }

    private fun CrearCuenta(email: String, password: String, rol: String) {
        progressDialog.setMessage("Creando cuenta")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                AgregarInfoDB()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo crear la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun AgregarInfoDB() {
        progressDialog.setMessage("Guardando información")
        val tiempo = System.currentTimeMillis()
        val uid = firebaseAuth.uid
        val datos_admin: HashMap<String, Any?> = HashMap()
        datos_admin["uid"] = uid
        datos_admin["nombre"] = nombre
        datos_admin["email"] = email
        datos_admin["rol"] = rol
        datos_admin["tiempo_registro"] = tiempo
        datos_admin["imagen"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Usuario")
        ref.child(uid!!)
            .setValue(datos_admin)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Cuenta creada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegistrarUsuario, MenuAdministrador::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "No se pudo guardar la información debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}