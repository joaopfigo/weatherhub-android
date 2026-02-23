package com.joaopedro.weatherhubandroid.rede

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi { //sempre que chamar o OpenWeatherApi, faz requisição para o endpoint "data/2.5/weather" e passa os parâmetros necessários para a API.
    @GET("data/2.5/weather")
    //suspend fun indica que pode ser chamada dentro de uma coroutine, operações em paralelo.
    suspend fun buscarClimaAtual(@Query("q") cidade: String, @Query("appid") chaveApi: String, @Query("units") unidades: String = "metric", @Query("lang") idioma: String = "pt_br"): RespostaClima

    @GET("data/2.5/forecast")
    suspend fun buscarPrevisao5Dias(@Query("q") cidade: String, @Query("appid") chaveApi: String, @Query("units") units: String = "metric", @Query("lang") lang: String = "pt_br"): RespostaPrevisao
}//A resposta da API será mapeada para a classe RespostaClima.