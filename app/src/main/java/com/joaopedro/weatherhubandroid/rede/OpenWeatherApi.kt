package com.joaopedro.weatherhubandroid.rede

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun buscarClimaAtual(
        @Query("q") cidade: String,
        @Query("appid") chaveApi: String,
        @Query("units") unidades: String = "metric",
        @Query("lang") idioma: String = "pt_br"
    ): RespostaClima
}