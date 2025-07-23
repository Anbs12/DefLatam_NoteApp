package com.example.deflatam_noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.deflatam_noteapp.R
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.model.Nota

class NotasAdapter(
    private val onNotaClick: (Nota) -> Unit
) : RecyclerView.Adapter<NotasAdapter.NotaViewHolder>() {

    private val notas = mutableListOf<Nota>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        holder.bind(notas[position])
    }

    override fun getItemCount(): Int = notas.size

    inner class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(nota: Nota) {
            val tvTitulo = itemView.findViewById<TextView>(R.id.tvTitulo)
            val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)

            // Mostrar el título o "Sin título" si está vacío
            tvTitulo.text = if (nota.titulo.isNotEmpty()) nota.titulo else "Sin título"

            // Formatear y mostrar la fecha
            tvFecha.text = NotasManager.formatearFecha(nota.fechaCreacion)

            itemView.setOnClickListener {
                onNotaClick(nota)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun actualizarNotas(nuevasNotas: List<Nota>) {
        notas.clear()
        notas.addAll(nuevasNotas)
        notifyDataSetChanged()
    }
}