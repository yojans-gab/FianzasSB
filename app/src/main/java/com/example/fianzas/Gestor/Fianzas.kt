package com.example.fianzas.Gestor

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fianzas.Administrador.ModeloEmpresas
import com.example.fianzas.databinding.ActivityFianzasBinding
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

    // 1. NUEVO: Mapa para los logos
    private lateinit var logosMap: HashMap<String, String>

    private var empresaSeleccionadaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFianzasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        empresaArrayList = ArrayList()
        logosMap = HashMap() // Inicializar mapa
        fianzasArrayList = ArrayList()

        // 2. Pasamos el 'logosMap' al adaptador
        adaptadorFianza = AdaptadorFianza(this, fianzasArrayList, logosMap)

        binding.finzasRv.layoutManager = LinearLayoutManager(this)
        binding.finzasRv.adapter = adaptadorFianza

        // Configuración de filtros (InputType Null para que sea solo click)
        binding.actvEmpresaFiltro.inputType = android.text.InputType.TYPE_NULL
        binding.actvEmpresaFiltro.isFocusable = false
        binding.actvEmpresaFiltro.isClickable = true

        binding.actvEmpresaFiltro.setOnClickListener { mostrarDialogoSeleccionEmpresa() }
        binding.tilEmpresaFiltro.setEndIconOnClickListener { mostrarDialogoSeleccionEmpresa() }
        binding.actvEmpresaFiltro.setOnLongClickListener {
            this.empresaSeleccionadaId = null
            binding.actvEmpresaFiltro.setText("")
            cargarTodasLasFianzas()
            true
        }

        cargarEmpresasYConfigurarSelector()
        cargarTodasLasFianzas()
    }

    private fun cargarEmpresasYConfigurarSelector() {
        // Nota: No mostramos progressDialog aquí para no bloquear si ya se cargaron fianzas
        // o puedes usar uno ligero.

        val ref = FirebaseDatabase.getInstance().getReference("Empresa")
            .orderByChild("nombreEmpresa")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresaArrayList.clear()
                logosMap.clear() // Limpiamos el mapa

                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloEmpresas::class.java)
                    if (modelo != null) {
                        // A. Para el filtro
                        empresaArrayList.add(modelo)

                        // B. Para los logos (Llenamos el mapa: ID -> URL)
                        if (modelo.empresaId.isNotEmpty()) {
                            logosMap[modelo.empresaId] = modelo.urlLogoEmpresa
                        }
                    }
                }

                // 3. Notificar al adaptador que los logos han cambiado/cargado
                adaptadorFianza.notifyDataSetChanged()

                // Gestión de UI vacía
                if (empresaArrayList.isEmpty()) {
                    binding.actvEmpresaFiltro.hint = "No hay empresas"
                    binding.actvEmpresaFiltro.isEnabled = false
                    binding.tilEmpresaFiltro.isEnabled = false
                } else {
                    binding.actvEmpresaFiltro.isEnabled = true
                    binding.tilEmpresaFiltro.isEnabled = true
                    binding.actvEmpresaFiltro.hint = "Seleccionar empresa"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FianzasActivity", "Error al cargar empresas: ${error.message}")
            }
        })
    }

    // ... El resto de tus funciones (mostrarDialogo, cargarTodasLasFianzas, filtrar, safeEpoch)
    // se mantienen IGUALES que en tu código anterior. Solo asegúrate de copiar y pegarlas.

    private fun mostrarDialogoSeleccionEmpresa() {
        if (empresaArrayList.isEmpty()) {
            Toast.makeText(this, "No hay empresas disponibles.", Toast.LENGTH_SHORT).show()
            return
        }
        val nombresEmpresasArray = Array(empresaArrayList.size) { i -> empresaArrayList[i].nombreEmpresa }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una Empresa")
        builder.setItems(nombresEmpresasArray) { dialog, which ->
            val empresaSeleccionada = empresaArrayList[which]
            this.empresaSeleccionadaId = empresaSeleccionada.empresaId
            binding.actvEmpresaFiltro.setText(empresaSeleccionada.nombreEmpresa)
            filtrarFianzasPorEmpresa(this.empresaSeleccionadaId)
            dialog.dismiss()
        }
        builder.show()
    }

    private fun cargarTodasLasFianzas() {
        progressDialog.setMessage("Cargando fianzas...")
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").orderByChild("fechaVencimiento")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fianzasArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloFianza::class.java)
                    if (modelo != null) {
                        if (modelo.fianzaId.isEmpty()) modelo.fianzaId = ds.key ?: ""
                        fianzasArrayList.add(modelo)
                    }
                }
                fianzasArrayList.sortWith(compareBy { safeEpochForSort(it.fechaVencimiento) })
                adaptadorFianza.notifyDataSetChanged()
                progressDialog.dismiss()
            }
            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun filtrarFianzasPorEmpresa(empresaId: String?) {
        if (empresaId.isNullOrEmpty()) {
            cargarTodasLasFianzas()
            return
        }
        progressDialog.setMessage("Cargando fianzas...")
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").orderByChild("empresaId").equalTo(empresaId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fianzasArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloFianza::class.java)
                    if (modelo != null) {
                        if (modelo.fianzaId.isEmpty()) modelo.fianzaId = ds.key ?: ""
                        fianzasArrayList.add(modelo)
                    }
                }
                fianzasArrayList.sortWith(compareBy { safeEpochForSort(it.fechaVencimiento) })
                adaptadorFianza.notifyDataSetChanged()
                progressDialog.dismiss()
            }
            override fun onCancelled(error: DatabaseError) { progressDialog.dismiss() }
        })
    }

    private fun safeEpochForSort(epoch: Long?): Long {
        if (epoch == null) return Long.MAX_VALUE
        return if (epoch <= 0L) Long.MAX_VALUE else {
            if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        }
    }
}