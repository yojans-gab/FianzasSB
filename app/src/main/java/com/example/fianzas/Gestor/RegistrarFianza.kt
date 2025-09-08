package com.example.fianzas.Gestor

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import java.util.Locale

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarFianzaBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat


class RegistrarFianza : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarFianzaBinding
    // Usaremos Calendar de java.util para mayor consistencia si hay dudas con minSDK
    private val calendarHelper: java.util.Calendar = java.util.Calendar.getInstance()

    private var fechaEmisionTimestamp: Long = 0
    private var fechaVencimientoTimestamp: Long = 0
    private var fechaNotificacionTimestamp: Long = 0 // Timestamp para la fecha de notificación
    private lateinit var empresaArrayList: ArrayList<ModeloEmpresas>

    // Constante para el tipo de fecha en mostrarDatePickerDialog
    private enum class TipoFecha { EMISION, VENCIMIENTO, NOTIFICACION }

    private var tipoFianzaSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el listener para el EditText de Tipo de Fianza
        binding.actvTipoFianza.setOnClickListener {
            seleccionarTipoFianza()
        }
        // Si usas un TextInputLayout con un ícono final, también puedes añadirle un listener:
        binding.tilTipoFianza.setEndIconOnClickListener { // Asumiendo que tilTipoFianza es el ID del TextInputLayout
            seleccionarTipoFianza()
        }

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

        binding.actvEmpresa.setOnClickListener {
            SeleccionarEmpresa()
        }
        binding.tilEmpresa.setEndIconOnClickListener {
            SeleccionarEmpresa()
        }
    }

    private fun seleccionarTipoFianza() {
        // 1. Cargar el string-array desde strings.xml
        val tiposFianzaArray: Array<String> = resources.getStringArray(R.array.TiposFianza)

        // 2. Crear y mostrar el AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona un Tipo de Fianza")
            .setItems(tiposFianzaArray) { dialog, which ->
                // 'which' es el índice del ítem seleccionado en el array
                tipoFianzaSeleccionado = tiposFianzaArray[which]
                binding.actvTipoFianza.setText(tipoFianzaSeleccionado) // Actualizar el EditText/TextView
                dialog.dismiss() // Cerrar el diálogo
            }
        builder.show()
    }
    private fun CargarEmpresas() {
        empresaArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Empresa").orderByChild("nombreEmpresa")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresaArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloEmpresas::class.java)
                    empresaArrayList.add(modelo!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private var idEmpresa = ""
    private var nombreEmpresa = ""


    private fun SeleccionarEmpresa() {
        val empresaArray = arrayOfNulls<String>(empresaArrayList.size)
        for (i in empresaArrayList.indices) {
            empresaArray[i] = empresaArrayList[i].nombreEmpresa
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una empresa").setItems(empresaArray) { dialog, which ->
            idEmpresa = empresaArrayList[which].empresaId
            nombreEmpresa = empresaArrayList[which].nombreEmpresa
            binding.actvEmpresa.setText(nombreEmpresa)
        }.show()

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
