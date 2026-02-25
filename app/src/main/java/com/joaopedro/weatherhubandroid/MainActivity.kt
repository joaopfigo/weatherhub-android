package com.joaopedro.weatherhubandroid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.*
import androidx.lifecycle.lifecycleScope
import coil.load
import com.joaopedro.weatherhubandroid.rede.FabricaRetrofit
import com.joaopedro.weatherhubandroid.rede.HistoricoCriarRequest
import com.joaopedro.weatherhubandroid.rede.FavoritoCriarRequest
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory

class MainActivity : AppCompatActivity() {
    companion object {
        private const val USER_ID_PADRAO = 1L
    }
    private var ultimaCidadeBuscada: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etCidade = findViewById<EditText>(R.id.etCidade)
        val prefs = getSharedPreferences("weatherhub", MODE_PRIVATE)
        val ultima = prefs.getString("ultima_cidade", null)
        if (!ultima.isNullOrBlank()) {
            etCidade.setText(ultima)
            ultimaCidadeBuscada = ultima
        }
        val btnBuscarClima = findViewById<Button>(R.id.btnBuscarClima)

        val txtTemperatura = findViewById<TextView>(R.id.txtTemperatura)
        val txtCondicao = findViewById<TextView>(R.id.txtCondicao)
        val txtExtras = findViewById<TextView>(R.id.txtExtras)
        val imgIconeClima = findViewById<ImageView>(R.id.imgIconeClima)

        val btnAbrirFavoritos = findViewById<Button>(R.id.btnAbrirFavoritos)
        val btnAbrirDetalhes = findViewById<Button>(R.id.btnAbrirDetalhes)
        var btnAdicionarFavoritoHome = findViewById<Button>(R.id.btnAdicionarFavoritoHome)
        var ultimoFavoritoRequest: FavoritoCriarRequest? = null

        btnBuscarClima.setOnClickListener {
            val cidade = etCidade.text.toString().trim()

            if (cidade.isEmpty()) {
                txtCondicao.text = "Digite uma cidade."
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = FabricaRetrofit.openWeatherApi()
                    val resposta = api.buscarClimaAtual(cidade = cidade, chaveApi = BuildConfig.OPENWEATHER_API_KEY)

                    ultimoFavoritoRequest = FavoritoCriarRequest(
                        cityName = resposta.name,
                        country = resposta.sys.country,
                        latitude = resposta.coord.lat,
                        longitude = resposta.coord.lon
                    )
                    ultimaCidadeBuscada = resposta.name
                    prefs.edit().putString("ultima_cidade", resposta.name).apply()

                    txtTemperatura.text = "Temperatura: ${resposta.main.temp} °C"
                    val condicao = resposta.weather.firstOrNull()?.description ?: "-"
                    txtCondicao.text = "Cidade: ${resposta.name} - País: ${resposta.sys.country}\nCondição: $condicao"
                    val historicoRequest = HistoricoCriarRequest(
                        cityName = resposta.name,
                        temperature = resposta.main.temp,
                        condition = resposta.weather.firstOrNull()?.description ?: "-"
                    )
                    try {
                        val apiHub = FabricaRetrofit.weatherHubApi()
                        apiHub.adicionarHistorico(USER_ID_PADRAO, historicoRequest)
                    } catch (e: Exception) {
                    }

                    txtExtras.text = "Umidade: ${resposta.main.humidity}% | Vento: ${resposta.wind.speed} m/s"

                    val icone = resposta.weather.firstOrNull()?.icon

                    imgIconeClima.setImageResource(R.drawable.ic_weather_placeholder)

                    if (!icone.isNullOrBlank()) {
                        val urlIcone = "https://openweathermap.org/img/wn/${icone}@2x.png"

                        val respIcone = api.baixarIcone(urlIcone)

                        if (respIcone.isSuccessful) {
                            val bytes = respIcone.body()?.bytes()

                            if (bytes != null && bytes.isNotEmpty()) {
                                imgIconeClima.load(bytes) {
                                    error(R.drawable.ic_weather_placeholder)
                                    fallback(R.drawable.ic_weather_placeholder)
                                }
                            } else {
                                imgIconeClima.setImageResource(R.drawable.ic_weather_placeholder)
                            }
                        } else {
                            imgIconeClima.setImageResource(R.drawable.ic_weather_placeholder)
                        }
                    }
                } catch (e: Exception) {
                    txtCondicao.text = "Erro ao buscar clima."
                    imgIconeClima.setImageDrawable(null)
                }
            }
        }

        btnAbrirFavoritos.setOnClickListener {
            startActivity(Intent(this, Favoritos::class.java))
        }

        btnAbrirDetalhes.setOnClickListener {
            val cidade = ultimaCidadeBuscada?.trim()

            if (cidade.isNullOrEmpty()) {
                txtCondicao.text = "Faça uma busca primeiro, aí abre os detalhes."
                return@setOnClickListener
            }

            val it = Intent(this, Detalhes::class.java)
            it.putExtra("cidade", cidade)
            startActivity(it)
        }

        btnAdicionarFavoritoHome.setOnClickListener {
            val req = ultimoFavoritoRequest
            if (req == null) {
                txtCondicao.text = "Busque uma cidade primeiro para favoritar."
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val apiHub = FabricaRetrofit.weatherHubApi()
                    val existentes = apiHub.listarFavoritos(USER_ID_PADRAO)
                    val jaExiste = existentes.any { it.cityName.equals(req.cityName, ignoreCase = true) }
                    if (jaExiste) {
                        txtCondicao.text = "Essa cidade já está nos favoritos."
                        return@launch
                    }
                    apiHub.adicionarFavorito(USER_ID_PADRAO, req)
                    txtCondicao.text = "Adicionado aos favoritos: ${req.cityName}"
                } catch (e: Exception) {
                    txtCondicao.text = "Erro ao adicionar aos favoritos."
                }
            }
        }
    }
}