package com.example.fianzas.Gestor

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarFianzaBinding
import java.text.SimpleDateFormat


class RegistrarFianza : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarFianzaBinding
    // Usaremos Calendar de java.util para mayor consistencia si hay dudas con minSDK
    private val calendarHelper: java.util.Calendar = java.util.Calendar.getInstance()

    private var fechaEmisionTimestamp: Long = 0
    private var fechaVencimientoTimestamp: Long = 0
    private var fechaNotificacionTimestamp: Long = 0 // Timestamp para la fecha de notificación

    // Constante para el tipo de fecha en mostrarDatePickerDialog
    private enum class TipoFecha { EMISION, VENCIMIENTO, NOTIFICACION }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener para el EditText de Fecha de Emisión
        binding.etFechaEmision.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaEmision, TipoFecha.EMISION)
        }
        binding.tilFechaEmision.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaEmision, TipoFecha.EMISION)
        }

        // Configurar el listener para el EditText de Fecha de Vencimiento
        binding.etFechaVencimiento.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaVencimiento, TipoFecha.VENCIMIENTO)
        }
        binding.tilFechaVencimiento.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaVencimiento, TipoFecha.VENCIMIENTO)
        }

        // Configurar el listener para el EditText de Fecha de Notificación
        binding.etFechaNotificacion.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaNotificacion, TipoFecha.NOTIFICACION)
        }
        binding.tilFechaNotificacion.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaNotificacion, TipoFecha.NOTIFICACION)
        }

        binding.btnGuardarFianza.setOnClickListener {

        }

        CargarEmpresas()
    }

    private fun CargarEmpresas() {

    }

    private fun mostrarDatePickerDialog(editText: EditText, tipoFecha: TipoFecha) {
        val currentCalendar = java.util.Calendar.getInstance()


        if (editText.text.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = dateFormat.parse(editText.text.toString())
                if (date != null) {
                    currentCalendar.time = date
                }
            } catch (e: Exception) {
                // No hacer nada si el parseo falla, se usará la fecha actual
            }
        }

        val year = currentCalendar.get(java.util.Calendar.YEAR)
        val month = currentCalendar.get(java.util.Calendar.MONTH)
        val day = currentCalendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
                val selectedDateCalendar = java.util.Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDateCalendar.time))

                when (tipoFecha) {
                    TipoFecha.EMISION -> {
                        fechaEmisionTimestamp = selectedDateCalendar.timeInMillis
                    }
                    TipoFecha.VENCIMIENTO -> {
                        fechaVencimientoTimestamp = selectedDateCalendar.timeInMillis
                        // Calcular y establecer la fecha de notificación
                        calcularYEstablecerFechaNotificacion(selectedDateCalendar)
                    }
                    TipoFecha.NOTIFICACION -> {
                        fechaNotificacionTimestamp = selectedDateCalendar.timeInMillis
                    }
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun calcularYEstablecerFechaNotificacion(fechaVencimientoCalendar: java.util.Calendar) {
        // Crear una copia para no modificar la instancia original de fechaVencimientoCalendar
        val notificacionCalendar = fechaVencimientoCalendar.clone() as java.util.Calendar
        notificacionCalendar.add(java.util.Calendar.DAY_OF_YEAR, -15) // Restar 15 días

        fechaNotificacionTimestamp = notificacionCalendar.timeInMillis

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etFechaNotificacion.setText(dateFormat.format(notificacionCalendar.time))
    }
}
