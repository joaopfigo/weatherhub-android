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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val USER_ID_PADRAO = 1L
    }
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
        val btnBuscarClima = findViewById<Button>(R.id.btnBuscarClima)

        val txtTemperatura = findViewById<TextView>(R.id.txtTemperatura)
        val txtCondicao = findViewById<TextView>(R.id.txtCondicao)
        val txtExtras = findViewById<TextView>(R.id.txtExtras)
        val imgIconeClima = findViewById<ImageView>(R.id.imgIconeClima)

        val btnAbrirFavoritos = findViewById<Button>(R.id.btnAbrirFavoritos)
        val btnAbrirDetalhes = findViewById<Button>(R.id.btnAbrirDetalhes)
        var ultimaCidadeBuscada: String? = null

        btnBuscarClima.setOnClickListener {
            val cidade = etCidade.text.toString().trim() //pega o texto do EditText e remove espaços extras

            if (cidade.isEmpty()) {
                txtCondicao.text = "Digite uma cidade."
                return@setOnClickListener
            }

            lifecycleScope.launch { // Inicia uma coroutine (rotina em paralelo) para fazer a chamada de rede sem bloquear a UI
                try {
                    // Cria uma instância do Retrofit e chama HTTP/função de busca da cidade informada
                    val api = FabricaRetrofit.openWeatherApi()
                    val resposta = api.buscarClimaAtual(cidade = cidade, chaveApi = BuildConfig.OPENWEATHER_API_KEY)

                    // Atualiza a interface do usuário com os dados recebidos da API
                    txtTemperatura.text = "Temperatura: ${resposta.main.temp} °C"
                    txtCondicao.text = "Condição: ${resposta.weather.firstOrNull()?.description ?: "-"}"
                    val historicoRequest = HistoricoCriarRequest(
                        cityName = resposta.name,
                        temperature = resposta.main.temp,
                        condition = resposta.weather.firstOrNull()?.description ?: "-"
                    )
                    try {
                        val apiHub = FabricaRetrofit.weatherHubApi()
                        apiHub.adicionarHistorico(USER_ID_PADRAO, historicoRequest)
                    } catch (e: Exception) {
                        // Ignora ou mostra erro simples
                    }

                    txtExtras.text = "Umidade: ${resposta.main.humidity}% | Vento: ${resposta.wind.speed} m/s"

                    val icone = resposta.weather.firstOrNull()?.icon
                    if (!icone.isNullOrBlank()) { //se nao for vazio
                        val urlIcone = "https://openweathermap.org/img/wn/${icone}@2x.png" // URL para obter o ícone do clima
                        imgIconeClima.load(urlIcone) // load carrega a imagem do ícone usando a biblioteca Coil. Se usa Coil atravez do .load?
                    }
                    ultimaCidadeBuscada = cidade

                } catch (e: Exception) {
                    txtCondicao.text = "Erro ao buscar clima."
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
    }
}