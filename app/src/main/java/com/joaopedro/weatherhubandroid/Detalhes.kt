package com.joaopedro.weatherhubandroid

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.security.ProviderInstaller
import com.joaopedro.weatherhubandroid.rede.FabricaRetrofit
import com.joaopedro.weatherhubandroid.rede.ItemPrevisao
import kotlinx.coroutines.launch

class Detalhes : AppCompatActivity() {
    companion object {
        private const val USER_ID_PADRAO = 1L
        private const val EXTRA_CIDADE = "cidade"
    }
    private val itensPrevisao = mutableListOf<ItemPrevisao>()
    private lateinit var adapter: PrevisaoAdapter
    private var cidade: String = ""
    private lateinit var txtStatus: TextView
    private lateinit var txtClimaAtual: TextView
    private lateinit var txtHistorico: TextView
    private lateinit var imgIcone: ImageView
    private lateinit var btnLimparHistorico: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (_: Exception) { }

        txtStatus = findViewById(R.id.txtStatusDetalhes)
        txtClimaAtual = findViewById(R.id.txtClimaAtual)
        txtHistorico = findViewById(R.id.txtHistorico)
        imgIcone = findViewById(R.id.imgIconeDetalhes)
        btnLimparHistorico = findViewById(R.id.btnLimparHistorico)

        val rv = findViewById<RecyclerView>(R.id.rvPrevisao)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarDetalhes)

        adapter = PrevisaoAdapter(itensPrevisao)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        btnVoltar.setOnClickListener { finish() }
        btnLimparHistorico.setOnClickListener { limparHistorico() }

        cidade = intent.getStringExtra(EXTRA_CIDADE)?.trim().orEmpty()
        if (cidade.isBlank()) {
            txtStatus.text = "Abra Detalhes a partir de uma busca (falta a cidade)."
            return
        }

        carregarTela()
    }
    private fun carregarTela() {
        txtStatus.text = "Carregando: $cidade"

        lifecycleScope.launch {
            carregarHistorico()
            carregarClimaAtualEIcone()
            carregarPrevisao5Dias()

            txtStatus.text = "Previsao (5 dias) - $cidade"
        }
    }
    private suspend fun carregarHistorico() {
        try {
            val apiHub = FabricaRetrofit.weatherHubApi()
            val lista = apiHub.listarHistorico(USER_ID_PADRAO)

            val filtrado = lista.filter { h ->
                h.cityName.equals(cidade, ignoreCase = true)
            }

            val linhas = if (filtrado.isEmpty()) {
                listOf("sem registros para essa cidade")
            } else {
                filtrado.map { h -> "${h.searchedAt} | ${h.temperature}°C | ${h.condition}" }
            }

            txtHistorico.text = "Historico:\n" + linhas.joinToString("\n")
        } catch (_: Exception) {
            txtHistorico.text = "Historico:\n(erro ao carregar)"
        }
    }
    private suspend fun carregarClimaAtualEIcone() {
        try {
            val api = FabricaRetrofit.openWeatherApi()
            val clima = api.buscarClimaAtual(
                cidade = cidade,
                chaveApi = BuildConfig.OPENWEATHER_API_KEY
            )

            val desc = clima.weather.firstOrNull()?.description ?: "-"

            txtClimaAtual.text =
                "Clima atual: ${clima.main.temp} °C\n" +
                        "Condicao: $desc\n" +
                        "Umidade: ${clima.main.humidity}%\n" +
                        "Vento: ${clima.wind.speed} m/s"

            imgIcone.setImageResource(R.drawable.ic_weather_placeholder)
            val iconCode = clima.weather.firstOrNull()?.icon
            if (!iconCode.isNullOrBlank()) {
                val url = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                imgIcone.load(url) {
                    error(R.drawable.ic_weather_placeholder)
                    fallback(R.drawable.ic_weather_placeholder)
                }
            }
        } catch (_: Exception) {
            txtClimaAtual.text = "Erro ao buscar clima atual."
            imgIcone.setImageResource(R.drawable.ic_weather_placeholder)
        }
    }
    private suspend fun carregarPrevisao5Dias() {
        try {
            val api = FabricaRetrofit.openWeatherApi()
            val resp = api.buscarPrevisao5Dias(
                cidade = cidade,
                chaveApi = BuildConfig.OPENWEATHER_API_KEY
            )

            itensPrevisao.clear()
            itensPrevisao.addAll(resp.list)
            adapter.notifyDataSetChanged()
        } catch (_: Exception) {
            txtStatus.text = "Erro ao buscar previsao."
        }
    }
    private fun limparHistorico() {
        lifecycleScope.launch {
            try {
                val apiHub = FabricaRetrofit.weatherHubApi()
                apiHub.limparHistorico(USER_ID_PADRAO)

                carregarHistorico()
            } catch (_: Exception) {
                txtHistorico.text = "Historico:\n(erro ao limpar)"
            }
        }
    }
    private class PrevisaoAdapter(private val dados: List<ItemPrevisao>) :
        RecyclerView.Adapter<PrevisaoViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): PrevisaoViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return PrevisaoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PrevisaoViewHolder, position: Int) {
            val item = dados[position]
            val desc = item.weather.firstOrNull()?.description ?: "-"
            holder.titulo.text = item.dt_txt
            holder.subtitulo.text = "${item.main.temp} °C | $desc"
        }

        override fun getItemCount() = dados.size
    }
    private class PrevisaoViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(android.R.id.text1)
        val subtitulo: TextView = view.findViewById(android.R.id.text2)
    }
}
