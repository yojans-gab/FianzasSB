package com.example.fianzas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.Fragmentos_Fianzas.Fragment_recientes
import com.example.fianzas.Fragmentos_Fianzas.Fragment_todas
import com.example.fianzas.databinding.ActivityListaFianzasBinding
import com.google.firebase.auth.FirebaseAuth

class ListaFianzasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaFianzasBinding
    //private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityListaFianzasBinding.inflate(layoutInflater)

        setContentView(binding.root)
        verFragmentRecientes()
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Menu_hoy -> {
                    verFragmentRecientes()
                    true
                }
                R.id.Menu_todas -> {
                    verFragmentTodas()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun verFragmentRecientes() {
        val nombre_titulo = "Recientes"
        binding.titulo.text = nombre_titulo

        val fragment = Fragment_recientes()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.Fragment.id, fragment, "Recientes")
        fragmentTransaction.commit()
    }

    private fun verFragmentTodas() {
        val nombre_titulo = "Todas"
        binding.titulo.text = nombre_titulo

        val fragment = Fragment_todas()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.Fragment.id, fragment, "Todas")
        fragmentTransaction.commit()
    }
}