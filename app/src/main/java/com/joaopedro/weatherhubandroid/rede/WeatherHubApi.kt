package com.joaopedro.weatherhubandroid.rede

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WeatherHubApi {

    @GET("api/users/{userId}/favorites")
    suspend fun listarFavoritos(
        @Path("userId") userId: Long
    ): List<ModelFavorito>

    @POST("api/users/{userId}/favorites")
    suspend fun adicionarFavorito(
        @Path("userId") userId: Long,
        @Body favorito: FavoritoCriarRequest
    ): ModelFavorito

    @DELETE("api/users/{userId}/favorites/{cityId}")
    suspend fun removerFavorito(
        @Path("userId") userId: Long,
        @Path("cityId") cityId: Long
    )
}