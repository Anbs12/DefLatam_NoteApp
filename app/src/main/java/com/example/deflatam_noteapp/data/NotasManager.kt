package com.example.deflatam_noteapp.data

import com.example.deflatam_noteapp.model.Nota

/**
 * Singleton para gestionar la lista de notas en memoria.
 * Provee metodos para manipular y acceder a los datos de las notas.
 */
object NotasManager {

    /**Lista de notas en memoria*/
    private val notas = mutableListOf<Nota>()

    init {
        agregarNota(Nota(System.currentTimeMillis(), "Nota de ejemplo", "Este es el contenido de prueba."))
        agregarNota(Nota(System.currentTimeMillis(), "Recordatorio", "Editame o algo no lo se, averigualo"))
    }

    /** Obtener todas las notas ordenadas por fecha (más recientes primero)*/
    fun obtenerNotas(): List<Nota> = notas.sortedByDescending { it.fechaCreacion }

    /**Agregar una nueva nota*/
    fun agregarNota(nota: Nota) {
        notas.add(nota)
    }

    /** Eliminar una nota por ID*/
    fun eliminarNota(id: Long) {
        notas.removeIf { it.id == id }
    }

    /** Buscar notas por título o contenido*/
    fun buscarNotas(query: String): List<Nota> {
        return notas.filter {
            it.titulo.contains(query, ignoreCase = true) ||
                    it.contenido.contains(query, ignoreCase = true)
        }.sortedByDescending { it.fechaCreacion }
    }

    /** Obtener una nota específica por su ID*/
    fun obtenerNotaPorId(id: Long): Nota? {
        return notas.find { it.id == id }
    }

    /**Actualizar una nota existente*/
    fun actualizarNota(nuevaNota: Nota) {
        val index = notas.indexOfFirst { it.id == nuevaNota.id }
        if (index != -1) {
            notas[index] = nuevaNota
        }
    }
}