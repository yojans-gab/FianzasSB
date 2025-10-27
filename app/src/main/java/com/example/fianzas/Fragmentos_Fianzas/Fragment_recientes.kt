package com.example.fianzas.Fragmentos_Fianzas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fianzas.Fianza
import com.example.fianzas.FianzaAdapter
import com.example.fianzas.databinding.FragmentRecientesBinding
import com.google.firebase.database.*
import java.util.Calendar
import kotlin.collections.ArrayList

class Fragment_recientes : Fragment() {

    private var _binding: FragmentRecientesBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var fianzasArrayList: ArrayList<Fianza>
    private lateinit var fianzaAdapter: FianzaAdapter

    private var fianzasListener: ValueEventListener? = null

    companion object {
        private const val TAG = "Fragment_recientes"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            binding.recyclerViewFianzasRecientes.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewFianzasRecientes.setHasFixedSize(true)

            fianzasArrayList = arrayListOf()
            fianzaAdapter = FianzaAdapter(fianzasArrayList)
            binding.recyclerViewFianzasRecientes.adapter = fianzaAdapter

            getFianzasData()
        } catch (t: Throwable) {
            Log.e(TAG, "Error en onViewCreated: ${t.message}", t)
        }
    }

    private fun getFianzasData() {
        try {
            dbRef = FirebaseDatabase.getInstance().getReference("Fianza")

            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            // Convertimos a Double porque Query.startAt/endAt en Kotlin se resuelve con Double para números
            val query = dbRef.orderByChild("fechaNotificacion")
                .startAt(startOfDay.toDouble())
                .endAt(endOfDay.toDouble())

            // Remover listener anterior si existe (evitar listeners duplicados)
            fianzasListener?.let { query.removeEventListener(it) }

            fianzasListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        fianzasArrayList.clear()
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val fianza = child.getValue(Fianza::class.java)
                                if (fianza != null) {
                                    // si quieres asignar id desde la key:
                                    if (fianza.fianzaId.isBlank()) {
                                        fianza.fianzaId = child.key ?: ""
                                    }
                                    fianzasArrayList.add(fianza)
                                } else {
                                    Log.w(TAG, "Fianza nula en ${child.key}")
                                }
                            }
                        } else {
                            Log.d(TAG, "No hay fianzas para hoy")
                        }
                        fianzaAdapter.notifyDataSetChanged()
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error procesando snapshot: ${t.message}", t)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Consulta cancelada: ${error.message}")
                }
            }

            query.addValueEventListener(fianzasListener as ValueEventListener)

        } catch (t: Throwable) {
            Log.e(TAG, "Error getFianzasData: ${t.message}", t)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            fianzasListener?.let {
                dbRef.removeEventListener(it)
                fianzasListener = null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error removiendo listener: ${t.message}")
        }
        _binding = null
    }
}
