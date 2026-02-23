package com.joaopedro.weatherhubandroid.rede

data class ModelHistorico(
    val id: Long,
    val userId: Long,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val searchedAt: String
)

// corpo do POST (temperatura e condição vêm da resposta da OpenWeather)
data class HistoricoCriarRequest(
    val cityName: String,
    val temperature: Double,
    val condition: String
)