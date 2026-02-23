package com.joaopedro.weatherhubandroid.rede

data class ModelFavorito(
    val id: Long,
    val userId: Long,
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val addedAt: String? = null
)

// corpo do POST (o backend pega o userId pela URL)
data class FavoritoCriarRequest(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)