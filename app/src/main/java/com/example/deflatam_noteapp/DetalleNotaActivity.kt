package com.example.deflatam_noteapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.databinding.ActivityDetalleNotaBinding
import com.example.deflatam_noteapp.model.Nota
import com.example.deflatam_noteapp.utils.AlarmScheduler
import java.io.IOException
import java.util.Calendar
import java.util.Date

class DetalleNotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleNotaBinding
    private var nota: Nota? = null
    private var notaOriginalVacia = false

    companion object {
        const val EXTRA_NOTA_ID = "extra_nota_id"
    }

    // Lanzador para el Storage Access Framework (Exportar nota única)
    private val exportarNotaLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    nota?.let { nota ->
                        val contenido = NotasManager.exportarNotasAString(listOf(nota))
                        escribirContenidoEnUri(uri, contenido)
                    }
                }
            }
        }

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
            // Formatear y mostrar la fecha
            val recordatorio =
                "Recordatorio: " + NotasManager.formatearFecha(nota!!.recordatorio.time)
            binding.tvFechaRecordatorio.text = recordatorio
        } else {
            finish()
            return
        }

        initSpinner()
        initButtons()

    }

    /**
     * Lanza un Intent para que el usuario elija dónde guardar el archivo.
     * El nombre del archivo sugerido es el título de la nota.
     */
    private fun lanzarIntentExportacion() {
        val tituloYcontenido = binding.etTitulo.text.isEmpty() || binding.etContenido.text.isEmpty()
        if (tituloYcontenido) {
            Toast.makeText(this, "Sin titulo y descripcion. Imposible", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                val nombreArchivo = "${nota?.titulo?.replace(" ", "_") ?: "Nota"}.txt"
                putExtra(Intent.EXTRA_TITLE, nombreArchivo)
            }
            exportarNotaLauncher.launch(intent)
        }
    }

    /**
     * Escribe el contenido de texto en el archivo seleccionado por el usuario.
     * Reutiliza la misma lógica que MainActivity.
     */
    private fun escribirContenidoEnUri(uri: Uri, contenido: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contenido.toByteArray())
                Toast.makeText(this, "Nota exportada con éxito", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error al exportar la nota", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun initButtons() {
        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            guardarNota()
            sms("Nota guardada")
            finish()
        }

        // Botón eliminar
        binding.btnEliminar.setOnClickListener {
            nota?.let {
                NotasManager.eliminarNota(it.id)
                sms("Nota eliminada")
            }
            finish()
        }

        //Boton recordatorio
        binding.btnRecordatorio.setOnClickListener {
            val calendar = Calendar.getInstance()
            nota?.recordatorio?.let { fechaRecordatorio ->
                calendar.time = fechaRecordatorio
            }

            // 1. Mostrar DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    // El mes es 0-indexed, por lo que monthOfYear ya es correcto para Calendar
                    val selectedDateCalendar = Calendar.getInstance()
                    selectedDateCalendar.set(Calendar.YEAR, year)
                    selectedDateCalendar.set(Calendar.MONTH, monthOfYear)
                    selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    // 2. Mostrar TimePickerDialog después de seleccionar la fecha
                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, hourOfDay, minute ->
                            selectedDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            selectedDateCalendar.set(Calendar.MINUTE, minute)
                            selectedDateCalendar.set(
                                Calendar.SECOND,
                                0
                            ) // Opcional: resetear segundos

                            // Asignamos la fecha y hora combinadas al recordatorio de la nota
                            nota?.recordatorio = selectedDateCalendar.time

                            // Formateamos para mostrar al usuario
                            val selectedDateTimeString =
                                NotasManager.formatearFecha(selectedDateCalendar.time.time)
                            val recordatorioEstablecido =
                                "Recordatorio nuevo establecido: " + selectedDateTimeString
                            binding.tvFechaRecordatorio.text = recordatorioEstablecido
                            sms("Recordatorio establecido para: $selectedDateTimeString")
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true // true para formato de 24 horas, false para AM/PM
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        //Boton Exportar
        // Listener para el nuevo botón de exportar
        binding.botonExportarNota.setOnClickListener {
            if (nota != null) {
                lanzarIntentExportacion()
            } else {
                Toast.makeText(this, "Guarda la nota antes de exportar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initSpinner() {
        val categorias = NotasManager.obtenerCategoriasUnicas()
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.spinnerCategoriaDetalleNota.adapter = spinnerAdapter
        binding.spinnerCategoriaDetalleNota.setSelection(categorias.indexOf(nota?.categoria))
        binding.spinnerCategoriaDetalleNota.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    nota?.categoria = categorias[p2]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun guardarNota() {
        val titulo = binding.etTitulo.text.toString().trim()
        val contenido = binding.etContenido.text.toString().trim()

        // Si el título y el contenido son vacíos, elimina la nota
        if (titulo.isEmpty()) {
            if (!notaOriginalVacia) {
                nota?.let {
                    AlarmScheduler.cancel(this, it)
                    NotasManager.eliminarNota(it.id)
                }
            }
            return
        }

        // Si la nota ya existe, actualiza su título y contenido
        nota?.let { notaActual ->
            if (titulo.isNotEmpty() || contenido.isNotEmpty()) {
                notaActual.titulo = titulo
                notaActual.contenido = contenido
                NotasManager.actualizarNota(notaActual)
            } else if (notaOriginalVacia) {
                NotasManager.eliminarNota(notaActual.id)
            }
        }
        // Programar o reprogramar la alarma
        nota?.let {
            if (it.recordatorio != null && it.recordatorio!!.after(Date())) {
                AlarmScheduler.schedule(this, it)
                Toast.makeText(this, "Recordatorio programado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        guardarNota()
    }

    private fun sms(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
