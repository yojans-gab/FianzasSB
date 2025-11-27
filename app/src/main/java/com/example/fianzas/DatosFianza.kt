package com.example.fianzas

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.ui.semantics.text
//import androidx.compose.ui.semantics.text
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.databinding.ActivityDatosFianzaBinding
import java.text.SimpleDateFormat
import kotlin.text.format
import java.util.Date
import java.text.NumberFormat


class DatosFianza : AppCompatActivity() {
    private lateinit var binding: ActivityDatosFianzaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatosFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- RECUPERAR EL OBJETO FIANZA ---
        val fianza = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("FIANZA_SELECCIONADA", Fianza::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("FIANZA_SELECCIONADA") as? Fianza
        }

        // Si la fianza no es nula, poblamos las vistas
        fianza?.let {

            val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "GT"))

            binding.tvDetalleNombreProyecto.text = it.nombreProyecto ?: "No disponible"
            binding.tvDetalleTipoFianza.text = it.tipoFianza ?: "No disponible"
            binding.tvDetalleNog.text = "NOG: ${it.nog ?: "N/A"}"
            binding.tvDetalleEstado.text = when(it.estado) {
                "1" -> "Estado: Por notificar"
                "2" -> "Estado: Notificada"
                else -> "Estado: N/A"
            }
            binding.tvDetalleFechaEmision.text = "Fecha de Emisión: ${formatDate(it.fechaEmision)}"
            binding.tvDetalleFechaVencimiento.text = "Fecha de Vencimiento: ${formatDate(it.fechaVencimiento)}"
            binding.TvMontoFianza.text = formatoMoneda.format(it.monto)
            binding.TvBeneficiarioFianza.text = it.beneficiario ?: "No especificado"
            binding.TvFiadorFianza.text = it.fiador ?: "No especificado"
        }
    }

    // Función para formatear la fecha, ahora con las clases correctas
    private fun formatDate(epoch: Long): String {
        if (epoch <= 0L) return "N/A"
        val ms = if (epoch < 1_000_000_000_000L) epoch * 1000L else epoch
        return try {
            // Usa el Locale de Java
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            // 2. USA EL 'Date' CORRECTO DE JAVA
            sdf.format(Date(ms))
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }
}
