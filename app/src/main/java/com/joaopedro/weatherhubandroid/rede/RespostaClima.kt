package com.joaopedro.weatherhubandroid.rede

data class RespostaClima(
    val name: String,                  // nome da cidade que a API devolve
    val main: InformacoesPrincipais,    // temp e umidade
    val weather: List<InformacoesClima>,// descrição e ícone
    val wind: InformacoesVento          // velocidade do vento
)

data class InformacoesPrincipais(
    val temp: Double,
    val humidity: Int
)

data class InformacoesClima(
    val description: String,
    val icon: String
)

data class InformacoesVento(
    val speed: Double
)