package com.example.fianzas.Gestor

import android.app.AlertDialog // No parece usarse aquí, pero lo mantengo por si acaso
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.setText

import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityFianzasBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fianzas.Administrador.ModeloEmpresas
import com.example.fianzas.Gestor.ModeloFianza
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Fianzas : AppCompatActivity() {

    private lateinit var binding: ActivityFianzasBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private lateinit var empresaArrayList: ArrayList<ModeloEmpresas>
    private lateinit var adaptadorFianza: AdaptadorFianza
    private lateinit var fianzasArrayList: ArrayList<ModeloFianza>
    private var empresaSeleccionadaId: String? = null // Para guardar el ID de la empresa seleccionada para filtrar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFianzasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        empresaArrayList = ArrayList()

        //Fianzas
        fianzasArrayList = ArrayList()
        adaptadorFianza = AdaptadorFianza(this, fianzasArrayList)
        binding.finzasRv.layoutManager = LinearLayoutManager(this)
        binding.finzasRv.adapter = adaptadorFianza


        // Para que actúe como un selector y no muestre teclado:
        binding.actvEmpresaFiltro.inputType = android.text.InputType.TYPE_NULL
        binding.actvEmpresaFiltro.isFocusable = false
        binding.actvEmpresaFiltro.isClickable = true

        binding.actvEmpresaFiltro.setOnClickListener {
            mostrarDialogoSeleccionEmpresa()
        }
        binding.tilEmpresaFiltro.setEndIconOnClickListener {
            mostrarDialogoSeleccionEmpresa()
        }
        binding.actvEmpresaFiltro.setOnLongClickListener {
            // limpiar filtro
            this.empresaSeleccionadaId = null
            binding.actvEmpresaFiltro.setText("")
            cargarTodasLasFianzas()
            true
        }


        cargarEmpresasYConfigurarSelector()

        cargarTodasLasFianzas()
    }

    private fun cargarEmpresasYConfigurarSelector() {
        progressDialog.setMessage("Cargando datos de empresas...")
        progressDialog.show()

        // Asegúrate que el nodo raíz de empresas sea "Empresas" (plural)
        val ref = FirebaseDatabase.getInstance().getReference("Empresa")
            .orderByChild("nombreEmpresa")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresaArrayList.clear()
                // Opción para "Todas las empresas" o un estado inicial sin filtro
                // Esto es opcional, dependiendo de tu UX
                // val todasLasEmpresas = ModeloEmpresas("TODAS", "Todas las Empresas", "", "")
                // empresaArrayList.add(todasLasEmpresas)


                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloEmpresas::class.java)
                    if (modelo != null) {
                        empresaArrayList.add(modelo)
                    } else {
                        Log.w("FianzasActivity", "Modelo de empresa nulo para la clave: ${ds.key}")
                    }
                }
                progressDialog.dismiss()

                // Si no hay empresas, puedes mostrar un mensaje o deshabilitar el selector
                if (empresaArrayList.isEmpty()) {
                    Toast.makeText(this@Fianzas, "No hay empresas registradas.", Toast.LENGTH_SHORT).show()
                    binding.actvEmpresaFiltro.hint = "No hay empresas" // Opcional
                    binding.actvEmpresaFiltro.isEnabled = false
                    binding.tilEmpresaFiltro.isEnabled = false
                } else {
                    binding.actvEmpresaFiltro.isEnabled = true
                    binding.tilEmpresaFiltro.isEnabled = true
                    binding.actvEmpresaFiltro.hint = "Seleccionar empresa" // Opcional
                    // No es necesario poblar el ArrayAdapter si usas un diálogo de selección
                    // Pero si quisieras un dropdown directo:
                    // configurarArrayAdapterEmpresas()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@Fianzas, "Error al cargar empresas: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("FianzasActivity", "Error Firebase al cargar empresas: ${error.message}")
            }
        })
    }

    private fun mostrarDialogoSeleccionEmpresa() {
        if (empresaArrayList.isEmpty()) {
            Toast.makeText(this, "No hay empresas disponibles para seleccionar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un array de solo nombres de empresas para el diálogo
        val nombresEmpresasArray = Array(empresaArrayList.size) { i -> empresaArrayList[i].nombreEmpresa }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una Empresa")
        builder.setItems(nombresEmpresasArray) { dialog, which ->
            val empresaSeleccionada = empresaArrayList[which]
            this.empresaSeleccionadaId = empresaSeleccionada.empresaId
            binding.actvEmpresaFiltro.setText(empresaSeleccionada.nombreEmpresa)
            filtrarFianzasPorEmpresa(this.empresaSeleccionadaId)   // <- aquí
            dialog.dismiss()
        }
        builder.show()
    }

    // Cargar todas las fianzas ordenadas por fechaVencimiento (más próximas primero)
    private fun cargarTodasLasFianzas() {
        progressDialog.setMessage("Cargando fianzas...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Fianza")
            .orderByChild("fechaVencimiento") // pedir orden por vencimiento

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fianzasArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloFianza::class.java)
                    if (modelo != null) {
                        if (modelo.fianzaId.isEmpty()) modelo.fianzaId = ds.key ?: ""
                        fianzasArrayList.add(modelo)
                    } else {
                        Log.w("FianzasActivity", "Fianza nula en ${ds.key}")
                    }
                }

                // Asegurar orden correcto en cliente (por si hay inconsistencias)
                fianzasArrayList.sortWith(compareBy { safeEpochForSort(it.fechaVencimiento) })

                adaptadorFianza.notifyDataSetChanged()
                progressDialog.dismiss()
                if (fianzasArrayList.isEmpty()) {
                    Toast.makeText(this@Fianzas, "No hay fianzas registradas.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Log.e("FianzasActivity", "Error cargar fianzas: ${error.message}")
                Toast.makeText(this@Fianzas, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Filtrar fianzas por empresaId y ordenar por fechaVencimiento en cliente
    private fun filtrarFianzasPorEmpresa(empresaId: String?) {
        if (empresaId.isNullOrEmpty()) {
            cargarTodasLasFianzas()
            return
        }
        progressDialog.setMessage("Cargando fianzas de la empresa...")
        progressDialog.show()

        val refQuery = FirebaseDatabase.getInstance().getReference("Fianza")
            .orderByChild("empresaId").equalTo(empresaId)

        refQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fianzasArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloFianza::class.java)
                    if (modelo != null) {
                        if (modelo.fianzaId.isEmpty()) modelo.fianzaId = ds.key ?: ""
                        fianzasArrayList.add(modelo)
                    }
                }

                // Ordenar en cliente por fechaVencimiento (más próximas primero)
                fianzasArrayList.sortWith(compareBy { safeEpochForSort(it.fechaVencimiento) })

                adaptadorFianza.notifyDataSetChanged()
                progressDialog.dismiss()
                if (fianzasArrayList.isEmpty()) {
                    Toast.makeText(this@Fianzas, "No hay fianzas para esta empresa.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Log.e("FianzasActivity", "Error filtrar fianzas: ${error.message}")
                Toast.makeText(this@Fianzas, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun safeEpochForSort(epoch: Long?): Long {
        if (epoch == null) return Long.MAX_VALUE
        return if (epoch <= 0L) Long.MAX_VALUE else {
            // si viene en segundos (menor a 1e12) lo convertimos a ms para comparar uniformemente
            if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        }
    }

}
