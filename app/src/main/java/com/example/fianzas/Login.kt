package com.example.fianzas // Asegúrate de que el paquete sea el correcto

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
// import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// import androidx.core.view.ViewCompat
// import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.Administrador.MenuAdministrador
import com.example.fianzas.Gestor.MenuGestor
import com.example.fianzas.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    // Definir constantes para los roles para evitar errores de tipeo
    companion object {
        const val ROL_ADMINISTRADOR = "Administrador"
        const val ROL_GESTOR = "Gestor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnLogin.setOnClickListener {
            validarInformacion()
        }

    }

    private fun comprobarSesionActiva() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            // Si ya hay sesión, intentar obtener el rol y redirigir
            obtenerRolYRedirigir(firebaseUser)
        }
    }


    private var email = ""
    private var password = ""

    private fun validarInformacion() {
        email = binding.EtEmailAdmin.text.toString().trim()
        password = binding.EtPasswordAdmin.text.toString().trim()

        if (email.isEmpty()) {
            binding.EtEmailAdmin.error = "Ingrese su email"
            binding.EtEmailAdmin.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmailAdmin.error = "Ingrese un email válido"
            binding.EtEmailAdmin.requestFocus()
        } else if (password.isEmpty()) {
            binding.EtPasswordAdmin.error = "Ingrese su contraseña"
            binding.EtPasswordAdmin.requestFocus()
        } else {
            loginUsuario()
        }
    }

    private fun loginUsuario() {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {authResult ->
                progressDialog.dismiss()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    obtenerRolYRedirigir(firebaseUser)
                } else {
                    // Esto no debería suceder si signInWithEmailAndPassword fue exitoso
                    Toast.makeText(this, "No se pudo obtener la información del usuario.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo iniciar sesión debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerRolYRedirigir(firebaseUser: FirebaseUser) {
        val uid = firebaseUser.uid
        val dbRef = FirebaseDatabase.getInstance().getReference("Usuario").child(uid)

        progressDialog.setMessage("Verificando usuario...")
        progressDialog.show()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (snapshot.exists()) {
                    val rolUsuario = snapshot.child("rol").getValue(String::class.java)

                    Log.d("Login", "Rol obtenido de DB: $rolUsuario") // Para depuración

                    when (rolUsuario) {
                        ROL_ADMINISTRADOR -> {
                            Toast.makeText(this@Login, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, MenuAdministrador::class.java))
                            finishAffinity()
                        }
                        ROL_GESTOR -> {
                            Toast.makeText(this@Login, "Bienvenido Gestor", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, MenuGestor::class.java)) // Cambia a tu Activity de Gestor
                            finishAffinity()
                        }
                        else -> {
                            Toast.makeText(this@Login, "Rol de usuario no reconocido o no asignado.", Toast.LENGTH_LONG).show()
                            // firebaseAuth.signOut()
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "No se encontraron datos adicionales para este usuario.", Toast.LENGTH_LONG).show()
                    firebaseAuth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@Login, "Error al leer datos del usuario: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

