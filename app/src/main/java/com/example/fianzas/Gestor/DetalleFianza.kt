package com.example.fianzas.Gestor

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityDetalleFianzaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleFianza : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleFianzaBinding
    private lateinit var progressDialog: ProgressDialog
    private var fianzaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de Toolbar (Flecha atrás)
        setSupportActionBar(binding.toolbarDetalle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle de Fianza"
        binding.toolbarDetalle.setNavigationOnClickListener { finish() }

        progressDialog = ProgressDialog(this).apply {
            setTitle("Cargando información...")
            setCanceledOnTouchOutside(false)
        }

        // Recuperar ID del Intent
        fianzaId = intent.getStringExtra("FIANZA_ID") ?: ""

        if (fianzaId.isNotEmpty()) {
            cargarDatosFianza()
        } else {
            Toast.makeText(this, "Error: ID no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cargarDatosFianza() {
        progressDialog.show()
        val ref = FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verificar si existe
                if (!snapshot.exists()) {
                    progressDialog.dismiss()
                    Toast.makeText(this@DetalleFianza, "Fianza no encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                // Convertir snapshot a ModeloFianza
                val modelo = snapshot.getValue(ModeloFianza::class.java)
                if (modelo == null) {
                    progressDialog.dismiss(); return
                }

                // Llenar UI con datos de la FIANZA
                binding.tvDetalleTipo.text = modelo.tipoFianza
                binding.tvDetalleNog.text = "NOG: ${modelo.nog}"
                binding.tvDetalleBeneficiario.text = modelo.beneficiario
                binding.tvDetalleProyecto.text = modelo.nombreProyecto
                binding.tvDetalleFiador.text = modelo.fiador

                // Formato Moneda (Q)
                val format = NumberFormat.getCurrencyInstance(Locale("es", "GT")) // Formato Quetzales o local
                binding.tvDetalleMonto.text = format.format(modelo.monto)

                // Formato Fechas
                binding.tvDetalleFechaEmision.text = formatDate(modelo.fechaEmision)
                binding.tvDetalleFechaVencimiento.text = formatDate(modelo.fechaVencimiento)
                binding.tvDetalleFechaNotif.text = formatDate(modelo.fechaNotificacion)

                // Cargar datos de la EMPRESA asociada (Nombre y Logo)
                cargarDatosEmpresa(modelo.empresaId)
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun cargarDatosEmpresa(empresaId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Empresa").child(empresaId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss() // Terminó toda la carga

                val nombreEmpresa = snapshot.child("nombreEmpresa").getValue(String::class.java) ?: "Empresa no encontrada"
                val urlLogo = snapshot.child("urlLogoEmpresa").getValue(String::class.java) ?: ""

                binding.tvDetalleNombreEmpresa.text = nombreEmpresa

                // Cargar Logo con Glide
                try {
                    Glide.with(this@DetalleFianza)
                        .load(urlLogo)
                        .placeholder(R.drawable.ic_empresa) // Usa tu drawable por defecto
                        .error(R.drawable.ic_empresa)
                        .centerCrop()
                        .into(binding.ivDetalleLogo)
                } catch (e: Exception) {}
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
            }
        })
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        // Asegurar milisegundos
        val ms = if (timestamp < 1_000_000_000_000L) timestamp * 1000L else timestamp
        val sdf = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "ES"))
        return sdf.format(Date(ms))
    }
}