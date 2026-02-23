package com.joaopedro.weatherhubandroid.rede

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FabricaRetrofit {
    private const val URL_BASE_OPENWEATHER = "https://api.openweathermap.org/"
    //essa URL vai ser completada com a interface do OpenWeatherApi que esta sendo chamada no coroutine do Main.
    fun openWeatherApi(): OpenWeatherApi { //herda a interface para ter acesso a buscarClimaAtual.
        return Retrofit.Builder()
            .baseUrl(URL_BASE_OPENWEATHER)
            .addConverterFactory(GsonConverterFactory.create()) //conversor de JSON para objetos Kotlin.
            .build()//constrói a instância do Retrofit com a URL base e o conversor.
            .create(OpenWeatherApi::class.java) //cria uma implementação da interface.
    }

    private const val URL_BASE_WEATHERHUB = "http://10.0.2.2:8080/"

    fun weatherHubApi(): WeatherHubApi {
        return Retrofit.Builder()
            .baseUrl(URL_BASE_WEATHERHUB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherHubApi::class.java)
    }
}