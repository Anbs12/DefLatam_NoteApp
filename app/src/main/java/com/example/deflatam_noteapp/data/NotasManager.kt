package com.example.deflatam_noteapp.data

import com.example.deflatam_noteapp.model.Nota
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Singleton para gestionar la lista de notas en memoria.
 * Provee metodos para manipular y acceder a los datos de las notas.
 */
object NotasManager {

    /**Lista de notas en memoria*/
    private val notas = mutableListOf<Nota>()
    private val categorias = mutableListOf<String>(
        "General",
        "Universidad",
        "Trabajo",
        "Vacaciones"
    )

    init {
        agregarNota(Nota(1, "Nota 1", "Contenido de la nota 1"))
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

    /** Formatear y mostrar la fecha
     * @param time La fecha en milisegundos.*/
    fun formatearFecha(time: Long): String{
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(time))
    }

    /**
     * Obtiene una lista de todas las categorías únicas existentes.
     * Útil para poblar un Spinner o un menú de filtros.
     */
    fun obtenerCategoriasUnicas(): List<String> {
        //return notas.map { it.categoria }.distinct().sorted()
        return categorias
    }

    /**
     * Obtiene todas las notas que pertenecen a una categoría específica.
     * Filtra la lista principal de notas por la categoría dada.
     */
    fun obtenerNotasPorCategoria(categoria: String): List<Nota> {
        return notas.filter { it.categoria.equals(categoria, ignoreCase = true) }
            .sortedByDescending { it.fechaCreacion }
    }

    /**
     * Obtiene las notas que tienen un recordatorio programado en el futuro.
     * Es la base para el sistema de notificaciones.
     */
    fun obtenerNotasConRecordatorioPendiente(): List<Nota> {
        val ahora = Date()
        return notas.filter { it.recordatorio != null && it.recordatorio!!.after(ahora) }
    }

    /**
     * Convierte una lista de notas a un único String formateado.
     * Ideal para exportar a un archivo de texto o compartir.
     */
    fun exportarNotasAString(listaDeNotas: List<Nota>): String {
        val builder = StringBuilder()
        val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

        builder.append("--- EXPORTACIÓN DE NOTAS ---\n\n")
        listaDeNotas.forEach { nota ->
            builder.append("TÍTULO: ${nota.titulo}\n")
            builder.append("FECHA: ${formatoFecha.format(nota.fechaCreacion)}\n")
            builder.append("CATEGORÍA: ${nota.categoria}\n")
            nota.recordatorio?.let {
                builder.append("RECORDATORIO: ${formatoFecha.format(it)}\n")
            }
            builder.append("CONTENIDO:\n${nota.contenido}\n")
            builder.append("----------------------------------------\n\n")
        }
        return builder.toString()
    }
}