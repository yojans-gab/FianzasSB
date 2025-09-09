package com.example.fianzas.Gestor

class ModeloFianza {
    var fianzaId: String = ""
    var empresaId: String = ""
    var nog: String = ""
    var tipoFianza: String = ""
    var fechaEmision: Long = 0
    var fechaVencimiento: Long = 0
    var fiador: String = ""
    var beneficiario: String = ""
    var nombreProyecto: String = ""
    var monto: Double = 0.0

    var fechaNotificacion: Long = 0
    var creadoPorUid: String = ""
    var estado: String = "1" // Valor por defecto

    constructor() {

    }

    constructor(
        fianzaId: String,
        empresaId: String,
        nog: String,
        tipoFianza: String,
        fechaEmision: Long,
        fechaVencimiento: Long,
        fiador: String,
        beneficiario: String,
        nombreProyecto: String,
        monto: Double,
        fechaNotificacion: Long,
        creadoPorUid: String,
        estado: String = "1" // Default value
    ) {
        this.fianzaId = fianzaId
        this.empresaId = empresaId
        this.nog = nog
        this.tipoFianza = tipoFianza
        this.fechaEmision = fechaEmision
        this.fechaVencimiento = fechaVencimiento
        this.fiador = fiador
        this.beneficiario = beneficiario
        this.nombreProyecto = nombreProyecto
        this.monto = monto
        this.fechaNotificacion = fechaNotificacion
        this.creadoPorUid = creadoPorUid
        this.estado = estado
    }
}