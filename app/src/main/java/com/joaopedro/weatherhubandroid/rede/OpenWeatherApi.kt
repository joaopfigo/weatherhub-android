package com.joaopedro.weatherhubandroid.rede

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface OpenWeatherApi {
    @GET("data/2.5/weather")
    //suspend fun indica que pode ser chamada dentro de uma coroutine, operações em paralelo.
    //retorna um objeto do tipo RespostaClima, que é a resposta da API do OpenWeather para a consulta do clima atual.
    suspend fun buscarClimaAtual(@Query("q") cidade: String, @Query("appid") chaveApi: String, @Query("units") unidades: String = "metric", @Query("lang") idioma: String = "pt_br"): RespostaClima

    @GET("data/2.5/forecast")
    suspend fun buscarPrevisao5Dias(@Query("q") cidade: String, @Query("appid") chaveApi: String, @Query("units") units: String = "metric", @Query("lang") lang: String = "pt_br"): RespostaPrevisao

    @Streaming
    @GET
    suspend fun baixarIcone(@Url urlCompleta: String): Response<ResponseBody>
}