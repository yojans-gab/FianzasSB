package com.example.fianzas.Administrador

class ModeloUsuarios {

    var email: String = ""
    var imagen: String = ""
    var nombre: String = ""
    var rol: String = ""
    var password: String = ""
    var tiempo: Long = 0
    var uid: String = ""

    constructor()
    constructor(email: String, imagen: String, nombre: String, rol: String, password: String, tiempo: Long, uid: String) {
        this.email = email
        this.imagen = imagen
        this.nombre = nombre
        this.rol = rol
        this.password = password
        this.tiempo = tiempo
        this.uid = uid
    }


}