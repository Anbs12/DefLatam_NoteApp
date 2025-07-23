package com.example.deflatam_noteapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deflatam_noteapp.adapter.NotasAdapter
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.databinding.ActivityMainBinding
import com.example.deflatam_noteapp.model.Nota
import com.example.deflatam_noteapp.utils.NotificationUtils
import com.example.deflatam_noteapp.utils.ThemeManager
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotasAdapter

    // Lanzador para el Storage Access Framework (Exportar)
    private val exportarLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val notasParaExportar = NotasManager.obtenerNotas()
                    val contenido = NotasManager.exportarNotasAString(notasParaExportar)
                    escribirContenidoEnUri(uri, contenido)
                }
            }
        }

    // Lanzador para solicitar permisos de notificación en Android 13+
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido.
            } else {
                smsPermission("No se concedieron los permisos de notificación")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))

        // Crear canal de notificación al iniciar la app
        NotificationUtils.createNotificationChannel(this)

        solicitarPermisosNotificacion()
        initComponents()
        initSearchView()
        initSpinnerCategorias()
        initButton()

    }

    override fun onResume() {
        super.onResume()
        // Refrescar lista al volver del detalle
        adapter.actualizarNotas(NotasManager.obtenerNotas())
        initSpinnerCategorias()
    }

    private fun initComponents() {
        // Inicializar adaptador
        adapter = NotasAdapter { nota ->
            val intent = Intent(this, DetalleNotaActivity::class.java)
            intent.putExtra("nota_id", nota.id)
            startActivity(intent)
        }

        // Configurar RecyclerView
        binding.rvNotas.layoutManager = LinearLayoutManager(this)
        binding.rvNotas.adapter = adapter
    }

    private fun initButton() {
        // Botón flotante para nueva nota
        binding.fabAgregarNota.setOnClickListener {
            val nuevaNotaId = System.currentTimeMillis()
            val nuevaNota = Nota(nuevaNotaId, "", "")

            // Guardar nota temporal
            NotasManager.agregarNota(nuevaNota)

            val intent = Intent(this, DetalleNotaActivity::class.java)
            intent.putExtra("nota_id", nuevaNota.id)
            startActivity(intent)
        }
        binding.botonExportarTodo.setOnClickListener {
            lanzarIntentExportacion()
        }

        binding.toolbarBusqueda.botonCambiarTemaClaroOscuro.setOnClickListener {
            mostrarDialogoDeTema()
        }
    }

    private fun initSearchView() {
        // Configurar búsqueda
        binding.toolbarBusqueda.searchView.queryHint = getString(R.string.buscar)
        binding.toolbarBusqueda.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtradas = NotasManager.buscarNotas(newText.orEmpty())
                adapter.actualizarNotas(filtradas)
                if (filtradas.isEmpty()) {
                    binding.tvSinNotas.visibility = View.VISIBLE
                } else {
                    binding.tvSinNotas.visibility = View.GONE
                }
                return true
            }
        })
    }

    private fun initSpinnerCategorias() {
        val categorias = NotasManager.obtenerCategoriasUnicas()
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        binding.toolbarBusqueda.spinnerCategoriaToolBar.adapter = spinnerAdapter
        binding.toolbarBusqueda.spinnerCategoriaToolBar.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val categoriaSeleccionada = categorias[position]
                    val notasFiltradas =
                        NotasManager.obtenerNotasPorCategoria(categoriaSeleccionada)
                    adapter.actualizarNotas(notasFiltradas)
                    if (notasFiltradas.isEmpty()) {
                        binding.tvSinNotas.visibility = View.VISIBLE
                    } else {
                        binding.tvSinNotas.visibility = View.GONE
                    }

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
    }

    private fun solicitarPermisosNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun smsPermission(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    /**
     * Muestra un diálogo para que el usuario elija el tema de la aplicación.
     * Las opciones son Claro, Oscuro o Predeterminado del sistema.
     */
    private fun mostrarDialogoDeTema() {
        val opciones = arrayOf("Claro", "Oscuro", "Predeterminado del sistema")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Tema")
        builder.setItems(opciones) { dialog, which ->
            val themeMode = when (which) {
                0 -> ThemeManager.THEME_LIGHT
                1 -> ThemeManager.THEME_DARK
                else -> ThemeManager.THEME_SYSTEM
            }
            ThemeManager.setTheme(this, themeMode)
        }
        builder.show()
    }

    /**
     * Lanza un Intent para que el usuario elija dónde guardar el archivo.
     * Usa el Storage Access Framework para crear un documento de texto.
     */
    private fun lanzarIntentExportacion() {
        if (NotasManager.obtenerNotas().isEmpty()) {
            Toast.makeText(this, "No hay notas para exportar", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "NotasExportadas.txt")
            }
            exportarLauncher.launch(intent)
        }
    }

    /**
     * Escribe el contenido de texto en el archivo seleccionado por el usuario.
     * Usa un OutputStream para escribir en la URI proporcionada.
     */
    private fun escribirContenidoEnUri(uri: Uri, contenido: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(contenido.toByteArray())
                Toast.makeText(this, "Notas exportadas con éxito", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error al exportar las notas", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}