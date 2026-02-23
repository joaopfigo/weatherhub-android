package com.joaopedro.weatherhubandroid.rede

data class RespostaClima(
    val name: String,// nome da cidade que a API devolve
    val main: ClimaMain, // temp e umidade
    val weather: List<ClimaInfo>,// descrição e ícone
    val wind: ClimaVento // velocidade do vento
)

data class ClimaMain(val temp: Double, val humidity: Int)
data class ClimaInfo(val description: String, val icon: String)
data class ClimaVento(val speed: Double)