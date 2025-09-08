package com.example.fianzas.Gestor

class ModeloEmpresas {
    var empresaId: String = ""
    var nombreEmpresa: String = ""
    var nombreDueno: String = ""
    var urlLogoEmpresa: String = "" // URL para la imagen del logo

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