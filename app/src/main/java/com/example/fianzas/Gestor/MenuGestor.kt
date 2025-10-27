package com.example.fianzas.Gestor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.ListaFianzasActivity
import com.example.fianzas.MainActivity
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityMenuGestorBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

class MenuGestor : AppCompatActivity() {

    private lateinit var binding: ActivityMenuGestorBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuGestorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        ComprobarSesion()

        binding.CerrarSesionGestor.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
        binding.cardCrearFinanzas.setOnClickListener {
            startActivity(Intent(this, RegistrarFianza::class.java))
        }
        binding.cardVerFinanzas.setOnClickListener {
            startActivity(Intent(this, Fianzas::class.java))
        }

        binding.cardNotificaciones.setOnClickListener {
            startActivity(Intent(this, ListaFianzasActivity::class.java))
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