package com.example.fianzas

import java.io.Serializable

/**
 * Modelo de datos que representa una Fianza.
 * Esta clase debe coincidir con la estructura de datos en Firebase Realtime Database.
 * Es 'Serializable' para poder pasarla entre Activities a través de Intents.
 */
data class Fianza(
    // 1. USA UNA SOLA PROPIEDAD PARA EL ID
    val id: String? = null,

    // El resto de tus propiedades
    val nog: String? = null,
    val estado: String? = null,
    val nombreProyecto: String? = null,
    val tipoFianza: String? = null,
    val fechaVencimiento: Long = 0L,
    val fechaEmision: Long = 0L
) : Serializable {

    // 2. CONSTRUCTOR VACÍO CORREGIDO
    // Debe coincidir con el número de parámetros del constructor principal (7 en este caso).
    constructor() : this(null, null, null, null, null, 0L, 0L)
}
