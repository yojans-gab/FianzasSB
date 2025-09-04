package com.example.fianzas.Administrador

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityUsuariosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Usuarios : AppCompatActivity() {

    private lateinit var binding: ActivityUsuariosBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context
    private lateinit var usuariosArrayList: ArrayList<ModeloUsuarios>
    private lateinit var adaptadorUsuarios: AdaptadorUsuarios


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        mContext = this

        LitaUsuarios()

    }

    private fun LitaUsuarios() {
        usuariosArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Usuario").orderByChild("nombre")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usuariosArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloUsuarios::class.java)
                    if (modelo != null) { // Comprobar si el modelo no es null
                        usuariosArrayList.add(modelo)
                    } else {
                        Log.w("UsuariosActivity", "Se encontró un usuario nulo o con datos incorrectos en Firebase: ${ds.key}")
                    }
                }
                adaptadorUsuarios = AdaptadorUsuarios(mContext, usuariosArrayList)
                binding.usuariosRv.adapter = adaptadorUsuarios
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UsuariosActivity", "Error al cargar usuarios: ${error.message}")
                Toast.makeText(mContext, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}