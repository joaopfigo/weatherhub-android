package com.joaopedro.weatherhubandroid

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.joaopedro.weatherhubandroid.rede.FabricaRetrofit
import com.joaopedro.weatherhubandroid.rede.ItemPrevisao
import kotlinx.coroutines.launch
import coil.load

class Detalhes : AppCompatActivity() {

    private val itens = mutableListOf<ItemPrevisao>()
    private lateinit var adapter: PrevisaoAdapter

    companion object {
        private const val USER_ID_PADRAO = 1L // ID fixo para simular um usuário único
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        val txtStatus = findViewById<TextView>(R.id.txtStatusDetalhes)
        val rv = findViewById<RecyclerView>(R.id.rvPrevisao)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarDetalhes)
        val txtClimaAtual = findViewById<TextView>(R.id.txtClimaAtual)
        val imgIconeDetalhes = findViewById<ImageView>(R.id.imgIconeDetalhes)

        btnVoltar.setOnClickListener { finish() }

        adapter = PrevisaoAdapter(itens)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val cidade = intent.getStringExtra("cidade")?.trim()
        if (cidade.isNullOrEmpty()) {
            txtStatus.text = "Abra Detalhes a partir de uma busca (falta a cidade)."
            return
        }

        txtStatus.text = "Carregando previsão de: $cidade"

        lifecycleScope.launch {
            try {
                val txtHistorico = findViewById<TextView>(R.id.txtHistorico)

                val apiHub = FabricaRetrofit.weatherHubApi()
                val listaHist = apiHub.listarHistorico(USER_ID_PADRAO)

                val filtrado = listaHist.filter { it.cityName.equals(cidade, ignoreCase = true) }
                txtHistorico.text =
                    if (filtrado.isEmpty()) "Histórico: sem registros"
                    else filtrado.joinToString("\n") { h ->
                        "${h.searchedAt} | ${h.temperature}°C | ${h.condition}"
                    }

                val api = FabricaRetrofit.openWeatherApi()

                val climaAtual = api.buscarClimaAtual(
                    cidade = cidade,
                    chaveApi = BuildConfig.OPENWEATHER_API_KEY
                )

                val cond = climaAtual.weather.firstOrNull()?.description ?: "-"
                txtClimaAtual.text =
                    "Clima atual: ${climaAtual.main.temp} °C\n" +
                            "Condição: $cond\n" +
                            "Umidade: ${climaAtual.main.humidity}%\n" +
                            "Vento: ${climaAtual.wind.speed} m/s"

                val icone = climaAtual.weather.firstOrNull()?.icon
                if (!icone.isNullOrBlank()) {
                    val url = "https://openweathermap.org/img/wn/${icone}@2x.png"
                    imgIconeDetalhes.load(url)
                }

                val resp = api.buscarPrevisao5Dias(
                    cidade = cidade,
                    chaveApi = BuildConfig.OPENWEATHER_API_KEY
                )

                itens.clear()
                itens.addAll(resp.list)
                adapter.notifyDataSetChanged()

                txtStatus.text = "Previsão (5 dias) - $cidade"
            } catch (e: Exception) {
                txtStatus.text = "Erro ao buscar dados."
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