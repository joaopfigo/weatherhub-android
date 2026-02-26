package com.joaopedro.weatherhubandroid.rede

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FabricaRetrofit {
    private const val URL_BASE_OPENWEATHER = "https://api.openweathermap.org/"
    fun openWeatherApi(): OpenWeatherApi {
        return Retrofit.Builder()
            .baseUrl(URL_BASE_OPENWEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherApi::class.java)
    }

    private const val URL_BASE_WEATHERHUB = "http://192.168.0.197:8080/"

    fun weatherHubApi(): WeatherHubApi {
        return Retrofit.Builder()
            .baseUrl(URL_BASE_WEATHERHUB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherHubApi::class.java)
    }
}