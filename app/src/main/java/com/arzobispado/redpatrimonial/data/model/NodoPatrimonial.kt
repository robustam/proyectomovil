package com.arzobispado.redpatrimonial.data.model

data class NodoPatrimonial(
    val id: Int = 0,
    val nombre: String = "",
    val direccion: String = "",
    val categoria: String = "",
    val estadoOperativo: String = "Abierto",
    val latitud: Double = -33.4372,
    val longitud: Double = -70.6506,
    val imagenUrl: String = ""
)