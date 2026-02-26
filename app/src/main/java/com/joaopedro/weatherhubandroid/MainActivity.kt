package com.joaopedro.weatherhubandroid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.security.ProviderInstaller
import com.joaopedro.weatherhubandroid.rede.FabricaRetrofit
import com.joaopedro.weatherhubandroid.rede.FavoritoCriarRequest
import com.joaopedro.weatherhubandroid.rede.HistoricoCriarRequest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val USER_ID_PADRAO = 1L
        private const val PREFS_NAME = "weatherhub"
        private const val PREF_ULTIMA_CIDADE = "ultima_cidade"
    }
    private var ultimaCidadeBuscada: String? = null
    private var ultimoFavoritoRequest: FavoritoCriarRequest? = null
    private lateinit var etCidade: EditText
    private lateinit var txtTemperatura: TextView
    private lateinit var txtCondicao: TextView
    private lateinit var txtExtras: TextView
    private lateinit var imgIconeClima: ImageView

    private lateinit var btnBuscarClima: Button
    private lateinit var btnAbrirFavoritos: Button
    private lateinit var btnAbrirDetalhes: Button
    private lateinit var btnAdicionarFavoritoHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        aplicarInsets()
        instalarProviderTlsSePossivel()

        bindViews()
        restaurarUltimaCidade()
        configurarClicks()
    }
    private fun aplicarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun instalarProviderTlsSePossivel() {
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (_: Exception) { }
    }
    private fun bindViews() {
        etCidade = findViewById(R.id.etCidade)
        txtTemperatura = findViewById(R.id.txtTemperatura)
        txtCondicao = findViewById(R.id.txtCondicao)
        txtExtras = findViewById(R.id.txtExtras)
        imgIconeClima = findViewById(R.id.imgIconeClima)

        btnBuscarClima = findViewById(R.id.btnBuscarClima)
        btnAbrirFavoritos = findViewById(R.id.btnAbrirFavoritos)
        btnAbrirDetalhes = findViewById(R.id.btnAbrirDetalhes)
        btnAdicionarFavoritoHome = findViewById(R.id.btnAdicionarFavoritoHome)
    }
    private fun restaurarUltimaCidade() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val ultima = prefs.getString(PREF_ULTIMA_CIDADE, null)

        if (!ultima.isNullOrBlank()) {
            etCidade.setText(ultima)
            ultimaCidadeBuscada = ultima
        }
    }
    private fun salvarUltimaCidade(cidade: String) {
        ultimaCidadeBuscada = cidade
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(PREF_ULTIMA_CIDADE, cidade).apply()
    }
    private fun configurarClicks() {
        btnBuscarClima.setOnClickListener {
            // trim() remove espacos no comeco/fim.
            val cidade = etCidade.text.toString().trim()
            if (cidade.isEmpty()) {
                txtCondicao.text = "Digite uma cidade."
                return@setOnClickListener
            }

            // coroutine = tarefa em segundo plano para nao travar a tela.
            lifecycleScope.launch {
                buscarClimaEAtualizarTela(cidade)
            }
        }

        btnAbrirFavoritos.setOnClickListener {
            // Intent = mensageiro que abre a tela Favoritos.
            startActivity(Intent(this, Favoritos::class.java))
        }

        btnAbrirDetalhes.setOnClickListener {
            val cidade = ultimaCidadeBuscada?.trim()

            if (cidade.isNullOrEmpty()) {
                txtCondicao.text = "Faca uma busca para abrir os detalhes."
                return@setOnClickListener
            }

            val it = Intent(this, Detalhes::class.java)
            // Extra = pacote de dados para a outra tela.
            it.putExtra("cidade", cidade)
            startActivity(it)
        }

        btnAdicionarFavoritoHome.setOnClickListener {
            val req = ultimoFavoritoRequest

            if (req == null) {
                txtCondicao.text = "Busque uma cidade para favoritar."
                return@setOnClickListener
            }

            lifecycleScope.launch {
                adicionarFavorito(req)
            }
        }
    }
    private suspend fun buscarClimaEAtualizarTela(cidade: String) {
        val apiOpenWeather = FabricaRetrofit.openWeatherApi()

        try {
            val resposta = apiOpenWeather.buscarClimaAtual(
                cidade = cidade,
                chaveApi = BuildConfig.OPENWEATHER_API_KEY
            )

            aplicarRespostaNaTela(resposta)

            salvarUltimaCidade(resposta.name)
            ultimoFavoritoRequest = FavoritoCriarRequest(
                cityName = resposta.name,
                country = resposta.sys.country,
                latitude = resposta.coord.lat,
                longitude = resposta.coord.lon
            )

            atualizarIcone(resposta.weather.firstOrNull()?.icon)

            enviarHistoricoParaWeatherHub(resposta)

        } catch (e: Exception) {
            txtCondicao.text = "Erro ao buscar clima: ${e.javaClass.simpleName} - ${e.message}"
            imgIconeClima.setImageResource(R.drawable.ic_weather_placeholder)
        }
    }
    private fun aplicarRespostaNaTela(resposta: com.joaopedro.weatherhubandroid.rede.RespostaClima) {
        txtTemperatura.text = "Temperatura: ${resposta.main.temp} °C"

        // Elvis (?:) = se for nulo, usa "-".
        val condicao = resposta.weather.firstOrNull()?.description ?: "-"
        txtCondicao.text = "Cidade: ${resposta.name} - Pais: ${resposta.sys.country}\nCondicao: $condicao"

        txtExtras.text = "Umidade: ${resposta.main.humidity}% | Vento: ${resposta.wind.speed} m/s"
    }
    private fun atualizarIcone(iconCode: String?) {
        imgIconeClima.setImageResource(R.drawable.ic_weather_placeholder)

        if (iconCode.isNullOrBlank()) {
            return
        }

        val urlIcone = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

        imgIconeClima.load(urlIcone) {
            error(R.drawable.ic_weather_placeholder)
            fallback(R.drawable.ic_weather_placeholder)
        }
    }
    private suspend fun enviarHistoricoParaWeatherHub(resposta: com.joaopedro.weatherhubandroid.rede.RespostaClima) {
        val historicoRequest = HistoricoCriarRequest(
            cityName = resposta.name,
            temperature = resposta.main.temp,
            condition = resposta.weather.firstOrNull()?.description ?: "-"
        )

        try {
            val apiHub = FabricaRetrofit.weatherHubApi()
            apiHub.adicionarHistorico(USER_ID_PADRAO, historicoRequest)
        } catch (_: Exception) { }
    }
    private suspend fun adicionarFavorito(req: FavoritoCriarRequest) {
        try {
            val apiHub = FabricaRetrofit.weatherHubApi()

            val existentes = apiHub.listarFavoritos(USER_ID_PADRAO)
            val jaExiste = existentes.any {
                // ignoreCase = ignora maiusculas/minusculas.
                it.cityName.equals(req.cityName, ignoreCase = true) &&
                        it.country.equals(req.country, ignoreCase = true)
            }

            if (jaExiste) {
                txtCondicao.text = "Essa cidade ja esta nos favoritos."
                return
            }

            apiHub.adicionarFavorito(USER_ID_PADRAO, req)
            txtCondicao.text = "Adicionado aos favoritos: ${req.cityName}"

        } catch (e: Exception) {
            txtCondicao.text = "Erro ao adicionar aos favoritos: ${e.javaClass.simpleName} - ${e.message}"
        }
    }
}
