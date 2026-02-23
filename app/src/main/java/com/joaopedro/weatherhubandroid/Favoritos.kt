package com.joaopedro.weatherhubandroid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joaopedro.weatherhubandroid.rede.FabricaRetrofit
import com.joaopedro.weatherhubandroid.rede.FavoritoCriarRequest
import com.joaopedro.weatherhubandroid.rede.ModelFavorito
import kotlinx.coroutines.launch

class Favoritos : AppCompatActivity() {

    private val USER_ID_PADRAO = 1L

    private val favoritos = mutableListOf<ModelFavorito>()
    private lateinit var adapter: FavoritosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        val txtStatus = findViewById<TextView>(R.id.txtStatusFavoritos)
        val etCidade = findViewById<EditText>(R.id.etCidadeFavorito)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionarFavorito)
        val rv = findViewById<RecyclerView>(R.id.rvFavoritos)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarFavoritos)

        btnVoltar.setOnClickListener { finish() }

        adapter = FavoritosAdapter(
            favoritos = favoritos,
            aoClicarRemover = { favorito -> removerFavorito(favorito, txtStatus) }
        )
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnAdicionar.setOnClickListener {
            val cidade = etCidade.text.toString().trim()
            if (cidade.isEmpty()) {
                txtStatus.text = "Digite uma cidade para adicionar."
                return@setOnClickListener
            }
            adicionarFavoritoPorCidade(cidade, txtStatus)
        }

        listarFavoritos(txtStatus)
    }

    private fun listarFavoritos(txtStatus: TextView) {
        txtStatus.text = "Carregando favoritos..."
        lifecycleScope.launch {
            try {
                val api = FabricaRetrofit.weatherHubApi()
                val lista = api.listarFavoritos(USER_ID_PADRAO)

                favoritos.clear()
                favoritos.addAll(lista)
                adapter.notifyDataSetChanged()

                txtStatus.text = "Favoritos carregados: ${favoritos.size}"
            } catch (e: Exception) {
                txtStatus.text = "Erro ao listar favoritos."
            }
        }
    }

    private fun adicionarFavoritoPorCidade(cidade: String, txtStatus: TextView) {
        txtStatus.text = "Adicionando: $cidade..."
        lifecycleScope.launch {
            try {
                val openWeather = FabricaRetrofit.openWeatherApi()
                val clima = openWeather.buscarClimaAtual(
                    cidade = cidade,
                    chaveApi = BuildConfig.OPENWEATHER_API_KEY
                )

                val request = FavoritoCriarRequest(
                    cityName = clima.name,
                    country = "BR",
                    latitude = 0.0,
                    longitude = 0.0
                )

                val api = FabricaRetrofit.weatherHubApi()
                api.adicionarFavorito(USER_ID_PADRAO, request)

                listarFavoritos(txtStatus)
            } catch (e: Exception) {
                txtStatus.text = "Erro ao adicionar favorito."
            }
        }
    }

    private fun removerFavorito(favorito: ModelFavorito, txtStatus: TextView) {
        txtStatus.text = "Removendo: ${favorito.cityName}..."
        lifecycleScope.launch {
            try {
                val api = FabricaRetrofit.weatherHubApi()
                api.removerFavorito(USER_ID_PADRAO, favorito.id)
                listarFavoritos(txtStatus)
            } catch (e: Exception) {
                txtStatus.text = "Erro ao remover favorito."
            }
        }
    }

    private class FavoritosAdapter(
        private val favoritos: List<ModelFavorito>,
        private val aoClicarRemover: (ModelFavorito) -> Unit
    ) : RecyclerView.Adapter<FavoritosViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FavoritosViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return FavoritosViewHolder(view)
        }

        override fun onBindViewHolder(holder: FavoritosViewHolder, position: Int) {
            val fav = favoritos[position]
            holder.titulo.text = "${fav.cityName} - ${fav.country}"
            holder.subtitulo.text = "id=${fav.id} | lat=${fav.latitude} lon=${fav.longitude}"

            holder.itemView.setOnLongClickListener {
                aoClicarRemover(fav)
                true
            }
        }

        override fun getItemCount() = favoritos.size
    }

    private class FavoritosViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(android.R.id.text1)
        val subtitulo: TextView = view.findViewById(android.R.id.text2)
    }
}