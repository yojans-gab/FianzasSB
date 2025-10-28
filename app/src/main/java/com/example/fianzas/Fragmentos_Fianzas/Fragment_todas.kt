package com.example.fianzas.Fragmentos_Fianzas

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fianzas.DatosFianza
import com.example.fianzas.Fianza
import com.example.fianzas.FianzaAdapter
import com.example.fianzas.R
import com.example.fianzas.databinding.FragmentTodasBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.collections.getValue
import kotlin.io.path.exists

class Fragment_todas : Fragment() {

    private var _binding: FragmentTodasBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbRef: DatabaseReference
    private lateinit var fianzasArrayList: ArrayList<Fianza>
    private lateinit var fianzaAdapter: FianzaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        binding.recyclerViewFianzas.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFianzas.setHasFixedSize(true)


        fianzasArrayList = arrayListOf()

// --- CAMBIO CLAVE: Inicialización del Adapter con el listener ---
        fianzaAdapter = FianzaAdapter(fianzasArrayList) { fianzaSeleccionada ->
            // Este bloque se ejecuta cuando se hace clic en un ítem
            val intent = Intent(requireContext(), DatosFianza::class.java).apply {
                // Ponemos el objeto completo de la fianza en el Intent
                putExtra("FIANZA_SELECCIONADA", fianzaSeleccionada)
            }
            startActivity(intent)
        }

// El resto de tu código sigue igual
        binding.recyclerViewFianzas.adapter = fianzaAdapter
        getFianzasData()
    }

    private fun getFianzasData() {
        // Apuntamos al nodo "Fianza" en la base de datos
        dbRef = FirebaseDatabase.getInstance().getReference("Fianza")

        // AQUÍ ESTÁ LA LÓGICA CLAVE:
        // 1. Ordenamos por el hijo "estado".
        // 2. Filtramos para obtener solo los que son iguales a "2".
        val query = dbRef.orderByChild("estado").equalTo("2")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiamos la lista anterior para evitar duplicados
                fianzasArrayList.clear()

                if (snapshot.exists()) {
                    for (fianzaSnapshot in snapshot.children) {
                        // Convertimos el snapshot a nuestro objeto Fianza
                        val fianza = fianzaSnapshot.getValue(Fianza::class.java)
                        if (fianza != null) {
                            fianzasArrayList.add(fianza)
                        }
                    }
                    // Notificamos al adaptador que los datos han cambiado para que repinte la lista
                    fianzaAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error si la lectura es cancelada
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding para evitar fugas de memoria
    }
}