package com.joaopedro.weatherhubandroid.rede

data class RespostaPrevisao(
    val list: List<ItemPrevisao>
)

data class ItemPrevisao(
    val dt_txt: String,
    val main: PrevisaoMain,
    val weather: List<PrevisaoClima>
)
data class PrevisaoMain(val temp: Double)
data class PrevisaoClima(val description: String, val icon: String)