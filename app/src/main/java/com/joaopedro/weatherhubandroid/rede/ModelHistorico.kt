package com.joaopedro.weatherhubandroid.rede

data class ModelHistorico(
    val id: Long,
    val userId: Long,
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val searchedAt: String
)

data class HistoricoCriarRequest(
    val cityName: String,
    val temperature: Double,
    val condition: String
)