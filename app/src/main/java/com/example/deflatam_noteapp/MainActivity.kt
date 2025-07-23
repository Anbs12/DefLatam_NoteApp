package com.example.deflatam_noteapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deflatam_noteapp.adapter.NotasAdapter
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.databinding.ActivityMainBinding
import com.example.deflatam_noteapp.model.Nota

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initSearchView()
        initButton()

    }

    override fun onResume() {
        super.onResume()
        // Refrescar lista al volver del detalle
        adapter.actualizarNotas(NotasManager.obtenerNotas())
    }

    private fun initComponents(){
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

    private fun initButton(){
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
    }

    private fun initSearchView(){
        // Configurar búsqueda
        binding.toolbarBusqueda.searchView.queryHint = getString(R.string.buscar)
        binding.toolbarBusqueda.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtradas = NotasManager.buscarNotas(newText.orEmpty())
                adapter.actualizarNotas(filtradas)
                return true
            }
        })
    }
}