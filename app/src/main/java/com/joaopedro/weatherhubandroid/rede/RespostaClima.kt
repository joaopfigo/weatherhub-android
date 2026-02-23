package com.joaopedro.weatherhubandroid.rede

data class RespostaClima(
    val name: String,// nome da cidade que a API devolve
    val sys: Sys,
    val main: ClimaMain, // temp e umidade
    val weather: List<ClimaInfo>,// descrição e ícone
    val wind: ClimaVento, // velocidade do vento
    val coord: Coord
)

data class ClimaMain(val temp: Double, val humidity: Int)
data class ClimaInfo(val description: String, val icon: String)
data class ClimaVento(val speed: Double)
data class Sys(val country: String)
data class Coord(val lat: Double, val lon: Double)