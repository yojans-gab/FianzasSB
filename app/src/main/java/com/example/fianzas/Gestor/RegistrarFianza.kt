package com.example.fianzas.Gestor

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.requestFocus
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import com.example.fianzas.Administrador.ModeloEmpresas
import com.example.fianzas.R
import com.example.fianzas.databinding.ActivityRegistrarFianzaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale

class RegistrarFianza : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarFianzaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private val calendarHelper: java.util.Calendar = java.util.Calendar.getInstance()

    private var fechaEmisionTimestamp: Long = 0
    private var fechaVencimientoTimestamp: Long = 0
    private var fechaNotificacionTimestamp: Long = 0
    private lateinit var empresaArrayList: ArrayList<ModeloEmpresas>

    private enum class TipoFecha { EMISION, VENCIMIENTO, NOTIFICACION }

    private var tipoFianzaSeleccionado: String = ""
    private var idEmpresaSeleccionada: String = ""
    private var nombreEmpresaSeleccionada: String = "" // Aunque no se usa directamente en el modelo Fianza, es bueno tenerla
    private var nombreDuenoSeleccionado: String = "" // Este será el 'fiador'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarFianzaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        empresaArrayList = ArrayList()

        // ... (tus listeners existentes para seleccionarTipoFianza, mostrarDatePickerDialog, seleccionarEmpresa)

        binding.btnGuardarFianza.setOnClickListener {
            validarYGuardarDatos() // Renombrado para más claridad
        }

        cargarEmpresas()

        binding.actvTipoFianza.setOnClickListener {
            seleccionarTipoFianza()
        }
        binding.tilTipoFianza.setEndIconOnClickListener {
            seleccionarTipoFianza()
        }

        binding.etFechaEmision.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaEmision, TipoFecha.EMISION)
        }
        binding.tilFechaEmision.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaEmision, TipoFecha.EMISION)
        }

        binding.etFechaVencimiento.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaVencimiento, TipoFecha.VENCIMIENTO)
        }
        binding.tilFechaVencimiento.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaVencimiento, TipoFecha.VENCIMIENTO)
        }

        binding.etFechaNotificacion.setOnClickListener {
            mostrarDatePickerDialog(binding.etFechaNotificacion, TipoFecha.NOTIFICACION)
        }
        binding.tilFechaNotificacion.setEndIconOnClickListener {
            mostrarDatePickerDialog(binding.etFechaNotificacion, TipoFecha.NOTIFICACION)
        }
        binding.btnGuardarFianza.setOnClickListener {
            validarYGuardarDatos()
        }


        cargarEmpresas()

        binding.actvEmpresa.setOnClickListener {
            seleccionarEmpresa()
        }
        binding.tilEmpresa.setEndIconOnClickListener {
            seleccionarEmpresa()
        }

        binding.etFiador.isFocusable = false
        binding.etFiador.isClickable = false
    }

    private fun validarYGuardarDatos() {
        val nog = binding.etNOG.text.toString().trim()
        val beneficiario = binding.etBenificiario.text.toString().trim()
        val nombreProyecto = binding.etnNombreProyecto.text.toString().trim()
        val montoStr = binding.etMonto.text.toString().trim()

        // Validaciones
        if (idEmpresaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Seleccione una empresa", Toast.LENGTH_SHORT).show()
            binding.actvEmpresa.requestFocus()
            return
        }
        if (nog.isEmpty()) {
            binding.etNOG.error = "NOG requerido"
            binding.etNOG.requestFocus()
            return
        }
        if (tipoFianzaSeleccionado.isEmpty()) {
            Toast.makeText(this, "Seleccione un tipo de fianza", Toast.LENGTH_SHORT).show()
            binding.actvTipoFianza.requestFocus()
            return
        }
        if (fechaEmisionTimestamp == 0L) {
            Toast.makeText(this, "Seleccione la fecha de emisión", Toast.LENGTH_SHORT).show()
            // Podrías enfocar el campo de fecha si lo deseas
            return
        }
        if (fechaVencimientoTimestamp == 0L) {
            Toast.makeText(this, "Seleccione la fecha de vencimiento", Toast.LENGTH_SHORT).show()
            return
        }
        if (fechaVencimientoTimestamp <= fechaEmisionTimestamp) {
            Toast.makeText(this, "La fecha de vencimiento debe ser posterior a la fecha de emisión", Toast.LENGTH_LONG).show()
            return
        }
        // nombreDuenoSeleccionado (fiador) ya debería estar lleno si se seleccionó una empresa
        if (nombreDuenoSeleccionado.isEmpty() && idEmpresaSeleccionada.isNotEmpty()) {
            // Esto podría pasar si el modelo de empresa no tiene nombreDueno
            Toast.makeText(this, "El fiador (dueño de la empresa) no pudo ser determinado.", Toast.LENGTH_LONG).show()
            return
        }
        if (beneficiario.isEmpty()) {
            binding.etBenificiario.error = "Beneficiario requerido"
            binding.etBenificiario.requestFocus()
            return
        }
        if (nombreProyecto.isEmpty()) {
            binding.etnNombreProyecto.error = "Nombre del proyecto requerido"
            binding.etnNombreProyecto.requestFocus()
            return
        }
        if (montoStr.isEmpty()) {
            binding.etMonto.error = "Monto requerido"
            binding.etMonto.requestFocus()
            return
        }

        val montoDouble: Double
        try {
            montoDouble = montoStr.replace(',', '.').toDouble()
            if (montoDouble <= 0) {
                binding.etMonto.error = "El monto debe ser positivo"
                binding.etMonto.requestFocus()
                return
            }
        } catch (e: NumberFormatException) {
            binding.etMonto.error = "Monto inválido"
            binding.etMonto.requestFocus()
            return
        }

        if (fechaNotificacionTimestamp == 0L) {
            Toast.makeText(this, "La fecha de notificación no ha sido establecida", Toast.LENGTH_SHORT).show()
            // Considera si quieres que esto sea obligatorio o si tiene un valor por defecto
            return
        }
        if (fechaNotificacionTimestamp >= fechaVencimientoTimestamp) {
            Toast.makeText(this, "La fecha de notificación debe ser anterior a la fecha de vencimiento", Toast.LENGTH_LONG).show()
            return
        }


        val creadoPorUid = firebaseAuth.currentUser?.uid
        if (creadoPorUid == null) {
            Toast.makeText(this, "Error de autenticación. Intente iniciar sesión de nuevo.", Toast.LENGTH_LONG).show()
            return
        }

        // Si todas las validaciones pasan:
        guardarFianzaEnFirebase(
            idEmpresaSeleccionada,
            nog,
            tipoFianzaSeleccionado,
            fechaEmisionTimestamp,
            fechaVencimientoTimestamp,
            nombreDuenoSeleccionado, // fiador
            beneficiario,
            nombreProyecto,
            montoDouble,
            fechaNotificacionTimestamp,
            creadoPorUid
        )
    }

    private fun guardarFianzaEnFirebase(
        empresaId: String, nog: String, tipoFianza: String, fechaEmision: Long,
        fechaVencimiento: Long, fiador: String, beneficiario: String,
        nombreProyecto: String, monto: Double, fechaNotificacion: Long, creadoPorUid: String
    ) {
        progressDialog.setMessage("Guardando fianza...")
        progressDialog.show()

        val refFianzas = FirebaseDatabase.getInstance().getReference("Fianza") // Nodo "Fianzas"
        val fianzaId = refFianzas.push().key

        if (fianzaId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "No se pudo generar el ID para la fianza.", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaFianza = ModeloFianza(
            fianzaId = fianzaId,
            empresaId = empresaId,
            nog = nog,
            tipoFianza = tipoFianza,
            fechaEmision = fechaEmision,
            fechaVencimiento = fechaVencimiento,
            fiador = fiador,
            beneficiario = beneficiario,
            nombreProyecto = nombreProyecto,
            monto = monto,
            fechaNotificacion = fechaNotificacion,
            creadoPorUid = creadoPorUid,
            estado = "1" // Estado por defecto
        )

        refFianzas.child(fianzaId).setValue(nuevaFianza)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Fianza guardada exitosamente!", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al guardar la fianza: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        binding.actvEmpresa.setText("")
        idEmpresaSeleccionada = ""
        nombreEmpresaSeleccionada = ""
        nombreDuenoSeleccionado = ""
        binding.etFiador.setText("")

        binding.etNOG.setText("")
        binding.actvTipoFianza.setText("")
        tipoFianzaSeleccionado = ""

        binding.etFechaEmision.setText("")
        fechaEmisionTimestamp = 0L
        binding.etFechaVencimiento.setText("")
        fechaVencimientoTimestamp = 0L
        binding.etFechaNotificacion.setText("")
        fechaNotificacionTimestamp = 0L

        binding.etBenificiario.setText("")
        binding.etnNombreProyecto.setText("")
        binding.etMonto.setText("")

        // Opcional: enfocar el primer campo o hacer scroll
        binding.actvEmpresa.requestFocus()
        // Si tienes un ScrollView como padre, podrías hacer:
        // binding.miScrollView.smoothScrollTo(0,0)
    }

    private fun seleccionarTipoFianza() {
        val tiposFianzaArray: Array<String> = resources.getStringArray(R.array.TiposFianza)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona un Tipo de Fianza")
            .setItems(tiposFianzaArray) { dialog, which ->
                tipoFianzaSeleccionado = tiposFianzaArray[which]
                binding.actvTipoFianza.setText(tipoFianzaSeleccionado)
                dialog.dismiss()
            }
        builder.show()
    }

    private fun cargarEmpresas() {
        // Asegúrate que el nodo raíz de empresas sea "Empresas" (plural) si así lo tienes en Firebase
        val ref = FirebaseDatabase.getInstance().getReference("Empresa") // Cambiado a "Empresas"
            .orderByChild("nombreEmpresa")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresaArrayList.clear()
                for (ds in snapshot.children) {
                    val modelo = ds.getValue(ModeloEmpresas::class.java)
                    if (modelo != null) {
                        empresaArrayList.add(modelo)
                    } else {
                        Log.w("RegistrarFianza", "Modelo de empresa nulo para la clave: ${ds.key}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistrarFianza, "Error al cargar empresas: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("RegistrarFianza", "Error Firebase al cargar empresas: ${error.message}")
            }
        })
    }

    private fun seleccionarEmpresa() {
        if (empresaArrayList.isEmpty()) {
            Toast.makeText(this, "Cargando empresas, por favor espera...", Toast.LENGTH_SHORT).show()
            // Considera si necesitas llamar a cargarEmpresas() aquí o si es suficiente con el listener.
            // Si el usuario llega aquí muy rápido, podría no estar cargada aún.
            return
        }

        val empresaNombresArray = Array(empresaArrayList.size) { i -> empresaArrayList[i].nombreEmpresa }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una empresa")
            .setItems(empresaNombresArray) { dialog, which ->
                val empresaSeleccionada = empresaArrayList[which]

                idEmpresaSeleccionada = empresaSeleccionada.empresaId
                nombreEmpresaSeleccionada = empresaSeleccionada.nombreEmpresa
                nombreDuenoSeleccionado = empresaSeleccionada.nombreDueno // Se obtiene el dueño

                binding.actvEmpresa.setText(nombreEmpresaSeleccionada)
                binding.etFiador.setText(nombreDuenoSeleccionado) // Se establece el fiador
                dialog.dismiss()
            }
        builder.show()
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
                // No hacer nada si el parseo falla
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
                    TipoFecha.EMISION -> fechaEmisionTimestamp = selectedDateCalendar.timeInMillis
                    TipoFecha.VENCIMIENTO -> {
                        fechaVencimientoTimestamp = selectedDateCalendar.timeInMillis
                        calcularYEstablecerFechaNotificacion(selectedDateCalendar)
                    }
                    TipoFecha.NOTIFICACION -> fechaNotificacionTimestamp = selectedDateCalendar.timeInMillis
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun calcularYEstablecerFechaNotificacion(fechaVencimientoCalendar: java.util.Calendar) {
        val notificacionCalendar = fechaVencimientoCalendar.clone() as java.util.Calendar
        notificacionCalendar.add(java.util.Calendar.DAY_OF_YEAR, -15)
        fechaNotificacionTimestamp = notificacionCalendar.timeInMillis
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etFechaNotificacion.setText(dateFormat.format(notificacionCalendar.time))
    }
}
