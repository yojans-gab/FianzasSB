package com.example.fianzas.Administrador

class ModeloEmpresas {
    var empresaId: String = ""
    var nombreEmpresa: String = ""
    var nombreDueno: String = ""
    // Firebase buscará "urlLogoEmpresa" en la base de datos para llenar esto
    var urlLogoEmpresa: String = ""

    constructor() // Constructor vacío para Firebase

    constructor(
        empresaId: String,
        nombreEmpresa: String,
        nombreDueno: String,
        urlLogoEmpresa: String
    ) {
        this.empresaId = empresaId
        this.nombreEmpresa = nombreEmpresa
        this.nombreDueno = nombreDueno
        this.urlLogoEmpresa = urlLogoEmpresa
    }

}