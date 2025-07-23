package com.example.deflatam_noteapp.model

/**
 * Modelo de datos para representar una nota.*/
data class Nota(
    val id: Long,
    var titulo: String,
    var contenido: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)