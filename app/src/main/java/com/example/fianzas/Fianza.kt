package com.example.fianzas

data class Fianza(
    var fianzaId: String = "",
    var nog: String = "",
    var estado: String = "",
    var nombreProyecto: String = "",
    var tipoFianza: String = "",
    var fechaVencimiento: Long = 0L,
    var fechaEmision: Long = 0L,
    var fechaNotificacion: Long = 0L,
    var monto: Double = 0.0,
    var fiador: String = "",
    var beneficiario: String = ""
)
