package com.example.fianzas.Gestor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fianzas.databinding.ActivityEditarFianzaBinding // Asegúrate de que este nombre coincida con tu XML
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditarFianza : AppCompatActivity() {

    private lateinit var binding: ActivityEditarFianzaBinding
    private lateinit var progressDialog: ProgressDialog

    // Variables para manejar los datos
    private var fianzaId: String = ""
    private var fechaEmisionTimestamp: Long = 0
    private var fechaVencimientoTimestamp: Long = 0
    private var fechaNotificacionTimestamp: Long = 0

    // Enum para reutilizar el selector de fecha
    private enum class TipoFecha { EMISION, VENCIMIENTO, NOTIFICACION }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setTitle("Por favor espere")
            setCanceledOnTouchOutside(false)
        }

        // 1. Obtener el ID que viene del Adaptador
        fianzaId = intent.getStringExtra("Fianza_Id") ?: ""

        if (fianzaId.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió el ID de la fianza", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2. Cargar los datos actuales de Firebase
        cargarDatosFianza()

        // 3. Configurar los DatePickers (Selectores de fecha)
        configurarSelectoresFecha()

        // 4. Botón Guardar
        binding.btnEditarFianza.setOnClickListener {
            validarYActualizarFianza()
        }
    }

    private fun cargarDatosFianza() {
        progressDialog.setMessage("Cargando información...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()
                if (!snapshot.exists()) {
                    Toast.makeText(this@EditarFianza, "La fianza no existe", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                // Obtener datos (Manejo seguro de nulos)
                val nog = snapshot.child("nog").getValue(String::class.java) ?: ""
                val beneficiario = snapshot.child("beneficiario").getValue(String::class.java) ?: ""
                val proyecto = snapshot.child("nombreProyecto").getValue(String::class.java) ?: ""

                // El monto puede venir como Double, Long o String, lo manejamos con cuidado
                val montoObj = snapshot.child("monto").value
                val monto = montoObj.toString()

                // Timestamps
                fechaEmisionTimestamp = snapshot.child("fechaEmision").getValue(Long::class.java) ?: 0L
                fechaVencimientoTimestamp = snapshot.child("fechaVencimiento").getValue(Long::class.java) ?: 0L
                fechaNotificacionTimestamp = snapshot.child("fechaNotificacion").getValue(Long::class.java) ?: 0L

                // Asignar a la UI
                binding.etNOGEditarFianza.setText(nog)
                binding.etBenificiarioEditarFianza.setText(beneficiario)
                binding.etnNombreProyectoEditarFianza.setText(proyecto)
                binding.etMontoEditarFianza.setText(monto)

                // Convertir Timestamps a Texto legible (dd/MM/yyyy)
                binding.etFechaEmisionEditarFianza.setText(convertirTimestampAString(fechaEmisionTimestamp))
                binding.etFechaVencimientoEditarianza.setText(convertirTimestampAString(fechaVencimientoTimestamp)) // Ojo: usé el ID tal cual lo tienes en el XML (Editarianza)
                binding.etFechaNotificacionEditarFianza.setText(convertirTimestampAString(fechaNotificacionTimestamp))
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@EditarFianza, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configurarSelectoresFecha() {
        // Fecha Emisión
        binding.tilFechaEmisionEditarFianza.setEndIconOnClickListener {
            mostrarDatePicker(binding.etFechaEmisionEditarFianza, TipoFecha.EMISION)
        }
        binding.etFechaEmisionEditarFianza.setOnClickListener {
            mostrarDatePicker(binding.etFechaEmisionEditarFianza, TipoFecha.EMISION)
        }

        // Fecha Vencimiento
        binding.tilFechaVencimientoEditarFianza.setEndIconOnClickListener {
            mostrarDatePicker(binding.etFechaVencimientoEditarianza, TipoFecha.VENCIMIENTO)
        }
        binding.etFechaVencimientoEditarianza.setOnClickListener {
            mostrarDatePicker(binding.etFechaVencimientoEditarianza, TipoFecha.VENCIMIENTO)
        }

        // Fecha Notificación
        binding.tilFechaNotificacionEditarFianza.setEndIconOnClickListener {
            mostrarDatePicker(binding.etFechaNotificacionEditarFianza, TipoFecha.NOTIFICACION)
        }
        binding.etFechaNotificacionEditarFianza.setOnClickListener {
            mostrarDatePicker(binding.etFechaNotificacionEditarFianza, TipoFecha.NOTIFICACION)
        }
    }

    private fun mostrarDatePicker(editText: EditText, tipo: TipoFecha) {
        val calendario = Calendar.getInstance()

        // Si ya hay una fecha escrita, iniciar el calendario en esa fecha
        if (editText.text.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(editText.text.toString())
                if (date != null) calendario.time = date
            } catch (e: Exception) { }
        }

        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val nuevoCalendario = Calendar.getInstance()
            nuevoCalendario.set(selectedYear, selectedMonth, selectedDay)
            val fechaSeleccionada = nuevoCalendario.timeInMillis

            // Actualizar Texto
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            editText.setText(sdf.format(nuevoCalendario.time))

            // Actualizar variable Long correspondiente
            when (tipo) {
                TipoFecha.EMISION -> fechaEmisionTimestamp = fechaSeleccionada
                TipoFecha.VENCIMIENTO -> {
                    fechaVencimientoTimestamp = fechaSeleccionada
                    // Opcional: Recalcular notificación sugerida (15 días antes)
                    calcularNotificacionAutomatica(nuevoCalendario)
                }
                TipoFecha.NOTIFICACION -> fechaNotificacionTimestamp = fechaSeleccionada
            }

        }, year, month, day).show()
    }

    private fun calcularNotificacionAutomatica(calVencimiento: Calendar) {
        val calNotif = calVencimiento.clone() as Calendar
        calNotif.add(Calendar.DAY_OF_YEAR, -15) // Restar 15 días
        fechaNotificacionTimestamp = calNotif.timeInMillis

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etFechaNotificacionEditarFianza.setText(sdf.format(calNotif.time))
    }

    private fun validarYActualizarFianza() {
        // Obtener textos
        val nog = binding.etNOGEditarFianza.text.toString().trim()
        val beneficiario = binding.etBenificiarioEditarFianza.text.toString().trim()
        val proyecto = binding.etnNombreProyectoEditarFianza.text.toString().trim()
        val montoStr = binding.etMontoEditarFianza.text.toString().trim()

        // Validaciones
        if (nog.isEmpty()) { binding.etNOGEditarFianza.error = "Requerido"; return }
        if (beneficiario.isEmpty()) { binding.etBenificiarioEditarFianza.error = "Requerido"; return }
        if (proyecto.isEmpty()) { binding.etnNombreProyectoEditarFianza.error = "Requerido"; return }

        if (fechaEmisionTimestamp == 0L || fechaVencimientoTimestamp == 0L) {
            Toast.makeText(this, "Verifique las fechas", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaVencimientoTimestamp <= fechaEmisionTimestamp) {
            Toast.makeText(this, "La fecha de vencimiento debe ser mayor a la emisión", Toast.LENGTH_LONG).show()
            return
        }

        val montoDouble: Double
        try {
            montoDouble = montoStr.replace(",", ".").toDouble()
        } catch (e: NumberFormatException) {
            binding.etMontoEditarFianza.error = "Monto inválido"
            return
        }

        // Preparar mapa de actualización
        val hashMap = HashMap<String, Any>()
        hashMap["nog"] = nog
        hashMap["beneficiario"] = beneficiario
        hashMap["nombreProyecto"] = proyecto
        hashMap["monto"] = montoDouble
        hashMap["fechaEmision"] = fechaEmisionTimestamp
        hashMap["fechaVencimiento"] = fechaVencimientoTimestamp
        hashMap["fechaNotificacion"] = fechaNotificacionTimestamp

        // Actualizar en Firebase
        progressDialog.setMessage("Actualizando fianza...")
        progressDialog.show()

        FirebaseDatabase.getInstance().getReference("Fianza").child(fianzaId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Fianza actualizada correctamente", Toast.LENGTH_SHORT).show()
                finish() // Regresar a la lista
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun convertirTimestampAString(timestamp: Long): String {
        if (timestamp <= 0) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            sdf.format(Date(timestamp))
        } catch (e: Exception) { "" }
    }
}