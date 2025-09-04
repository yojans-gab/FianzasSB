package com.example.fianzas.Administrador

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.Login
import com.example.fianzas.MainActivity
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityMenuAdministradorBinding
import com.google.firebase.auth.FirebaseAuth

class MenuAdministrador : AppCompatActivity() {

    private lateinit var binding: ActivityMenuAdministradorBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuAdministradorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        ComprobarSesion()

        binding.CerrarSesionAdmin.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        binding.cardRegistrarUsuario.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            startActivity(intent)
        }

        binding.cardUsuarios.setOnClickListener {
            val intent = Intent(this, Usuarios::class.java)
            startActivity(intent)
        }


    }

    private fun ComprobarSesion() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
        else{
            Toast.makeText(applicationContext, "Bienvenido ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
        }
    }
}