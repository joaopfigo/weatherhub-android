package com.joaopedro.weatherhubandroid.rede

data class RespostaClima(
    val name: String,
    val sys: Sys,
    val main: ClimaMain,
    val weather: List<ClimaInfo>,
    val wind: ClimaVento,
    val coord: Coord
)

data class ClimaMain(val temp: Double, val humidity: Int)
data class ClimaInfo(val description: String, val icon: String)
data class ClimaVento(val speed: Double)
data class Sys(val country: String)
data class Coord(val lat: Double, val lon: Double)