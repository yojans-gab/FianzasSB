package com.example.fianzas.Administrador

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fianzas.Administrador.ModeloEmpresas
import com.example.fianzas.databinding.ActivityEmpresasBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Empresas : AppCompatActivity() {

    private lateinit var binding: ActivityEmpresasBinding
    private lateinit var empresasArrayList: ArrayList<ModeloEmpresas>
    private lateinit var adaptadorEmpresas: AdaptadorEmpresa
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpresasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        configurarRecyclerView()
        listarEmpresas()
    }

    private fun configurarRecyclerView() {
        empresasArrayList = ArrayList()
        adaptadorEmpresas = AdaptadorEmpresa(this, empresasArrayList)
        binding.empresasRv.layoutManager = LinearLayoutManager(this)
        binding.empresasRv.adapter = adaptadorEmpresas
    }

    private fun listarEmpresas() {
        // Asegúrate de usar el nodo correcto en Firebase (Empresas vs Empresa)
        val ref = FirebaseDatabase.getInstance().getReference("Empresa").orderByChild("nombreEmpresa")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresasArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloEmpresas::class.java)
                    if (modelo != null) {
                        // si no llega el ID desde la bd, asigna key:
                        if (modelo.empresaId.isEmpty()) modelo.empresaId = ds.key ?: ""
                        empresasArrayList.add(modelo)
                    } else {
                        Log.w("EmpresasActivity", "Empresa nula en: ${ds.key}")
                    }
                }
                adaptadorEmpresas.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("EmpresasActivity", "Error al cargar Empresas: ${error.message}")
                Toast.makeText(this@Empresas, "Error al cargar Empresas: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
