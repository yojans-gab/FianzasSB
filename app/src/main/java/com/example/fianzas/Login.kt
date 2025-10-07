package com.example.fianzas

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fianzas.Administrador.MenuAdministrador
import com.example.fianzas.Gestor.MenuGestor
import com.example.fianzas.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private val REQUEST_POST_NOTIFICATIONS = 101

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

        comprobarSesionActiva()
    }

    private fun comprobarSesionActiva() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            obtenerRolYRedirigir(firebaseUser)
        }
    }

    private var email = ""
    private var password = ""

    private fun validarInformacion() {
        email = binding.EtEmailAdmin.text.toString().trim()
        password = binding.EtPasswordAdmin.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.EtEmailAdmin.error = "Ingrese su email"
                binding.EtEmailAdmin.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.EtEmailAdmin.error = "Ingrese un email válido"
                binding.EtEmailAdmin.requestFocus()
            }
            password.isEmpty() -> {
                binding.EtPasswordAdmin.error = "Ingrese su contraseña"
                binding.EtPasswordAdmin.requestFocus()
            }
            else -> loginUsuario()
        }
    }

    private fun loginUsuario() {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                progressDialog.dismiss()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    obtenerRolYRedirigir(firebaseUser)
                } else {
                    Toast.makeText(this, "No se pudo obtener la información del usuario.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo iniciar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Log.d("Login", "Rol obtenido de DB: $rolUsuario")

                    when (rolUsuario) {
                        ROL_ADMINISTRADOR -> {
                            Toast.makeText(this@Login, "Bienvenido Administrador", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, MenuAdministrador::class.java))
                            finishAffinity()
                        }
                        ROL_GESTOR -> {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(
                                        this@Login,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    ActivityCompat.requestPermissions(
                                        this@Login,
                                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                        REQUEST_POST_NOTIFICATIONS
                                    )
                                } else {
                                    subscribeUserToGestoresTopic()
                                }
                            } else {
                                subscribeUserToGestoresTopic()
                            }

                            Toast.makeText(this@Login, "Bienvenido Gestor", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login, MenuGestor::class.java))
                            finishAffinity()
                        }
                        else -> {
                            Toast.makeText(this@Login, "Rol de usuario no reconocido o no asignado.", Toast.LENGTH_LONG).show()
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

    private fun subscribeUserToGestoresTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("gestores")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Subscribed to topic 'gestores'")
                } else {
                    Log.w("FCM", "Failed to subscribe to 'gestores'", task.exception)
                }
            }
    }

    private fun unsubscribeUserFromGestoresTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("gestores")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FCM", "Unsubscribed from topic 'gestores'")
                } else {
                    Log.w("FCM", "Failed to unsubscribe from 'gestores'", task.exception)
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                subscribeUserToGestoresTopic()
            } else {
                Toast.makeText(this, "Permiso de notificaciones denegado; no se recibirán alarmas.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
