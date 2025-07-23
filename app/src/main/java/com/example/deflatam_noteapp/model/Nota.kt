package com.example.deflatam_noteapp.model

import java.util.Date
import kotlin.random.Random

/**
 * Modelo de datos para representar una nota.*/
data class Nota(
    val id: Long = Random.nextLong(),
    var titulo: String,
    var contenido: String,
    val fechaCreacion: Long = System.currentTimeMillis(),
    var categoria: String = "General",
    var recordatorio: Date = Date()
)