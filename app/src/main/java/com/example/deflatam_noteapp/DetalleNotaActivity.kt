package com.example.deflatam_noteapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.databinding.ActivityDetalleNotaBinding
import com.example.deflatam_noteapp.model.Nota

class DetalleNotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleNotaBinding
    private var nota: Nota? = null
    private var notaOriginalVacia = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleNotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idNota = intent.getLongExtra("nota_id", -1)
        nota = NotasManager.obtenerNotaPorId(idNota)

        if (nota != null) {
            notaOriginalVacia = nota!!.titulo.isEmpty() && nota!!.contenido.isEmpty()

            binding.etTitulo.setText(nota!!.titulo)
            binding.etContenido.setText(nota!!.contenido)
        } else {
            finish()
            return
        }

        initButtons()

    }

    private fun initButtons() {
        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            guardarNota()
            if (notaOriginalVacia) {
                Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }
            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Botón eliminar
        binding.btnEliminar.setOnClickListener {
            nota?.let {
                NotasManager.eliminarNota(it.id)
                Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun guardarNota() {
        val titulo = binding.etTitulo.text.toString().trim()
        val contenido = binding.etContenido.text.toString().trim()

        nota?.let { notaActual ->
            if (titulo.isNotEmpty() || contenido.isNotEmpty()) {
                notaActual.titulo = titulo
                notaActual.contenido = contenido
                NotasManager.actualizarNota(notaActual)
            } else if (notaOriginalVacia) {
                NotasManager.eliminarNota(notaActual.id)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        guardarNota()
    }
}