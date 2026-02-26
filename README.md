# WeatherHubAndroid (Kotlin / Views) — App Mobile + Integração com OpenWeather e WeatherHub API

App Android em Kotlin (Views / Activities) que:

* Busca **clima atual** e **previsão de 5 dias** na **OpenWeather**.
* Salva e consulta **histórico** e **favoritos** no backend **WeatherHub API** (Spring Boot).
* Exibe **ícone do clima** via Coil.

---

## Conteúdo exigido no enunciado (imagem)

A imagem do enunciado pede que o projeto demonstre:

1. **Consumo de múltiplas APIs REST**
   Este app consome **duas APIs REST**:

   * OpenWeather (clima atual + forecast)
   * WeatherHub API (favoritos + histórico)

2. **Retrofit e OkHttp (configuração e uso)**

   * Retrofit é usado para definir interfaces (`OpenWeatherApi`, `WeatherHubApi`) e realizar chamadas HTTP.
   * GsonConverterFactory converte JSON ↔ Kotlin data classes automaticamente.
   * OkHttp está como dependência (base do Retrofit). Logging Interceptor está incluído nas dependências (pode ser ativado se necessário).

3. **Tratamento de responses assíncronos**

   * As chamadas de rede são feitas com **coroutines** (`lifecycleScope.launch`) e métodos `suspend`.
   * Isso evita travar a UI e permite atualizar a tela quando a resposta chega.

---

## Funcionalidades (fluxo do app)

### Home (MainActivity)

* Usuário digita a cidade e toca **Buscar clima**.
* Faz GET na OpenWeather e atualiza UI (temperatura, condição, umidade, vento).
* Carrega ícone com Coil.
* Faz POST no backend WeatherHub API salvando o histórico.
* Permite:

  * **Ir para Favoritos**
  * **Ir para Detalhes**
  * **Adicionar aos favoritos** (com base no último resultado de busca)

### Favoritos (Favoritos)

* Lista favoritos do backend (GET).
* Para cada favorito, busca a temperatura atual na OpenWeather.
* Abre Detalhes ao tocar no item.
* Remove favorito (DELETE).
* Adiciona favorito digitando cidade:

  * Busca a cidade na OpenWeather (para obter `country`, `lat`, `lon`)
  * Faz POST no backend criando favorito (sem duplicar).

### Detalhes (Detalhes)

* Recebe a cidade via `Intent`.
* Mostra:

  * Clima atual + ícone
  * Previsão 5 dias (RecyclerView)
  * Histórico filtrado daquela cidade (GET no backend e filtra)
* Botão para **limpar histórico** (DELETE /history) do usuário.

---

## Estrutura do projeto

Principais arquivos Kotlin:

* `MainActivity.kt` — Home (buscar clima, salvar histórico, favoritar)
* `Favoritos.kt` — Lista/Adicionar/Remover favoritos
* `Detalhes.kt` — Clima + forecast + histórico + limpar histórico

Pacote de rede (`app/src/main/java/com/joaopedro/weatherhubandroid/rede`):

* `FabricaRetrofit.kt` — cria Retrofits (OpenWeather e WeatherHub)
* `OpenWeatherApi.kt` — endpoints da OpenWeather
* `WeatherHubApi.kt` — endpoints do backend WeatherHub
* `RespostaClima.kt` — models do clima atual
* `RespostaPrevisao.kt` — models do forecast
* `ModelFavorito.kt` — models + request de favorito
* `ModelHistorico.kt` — models + request de histórico

Layouts:

* `activity_main.xml`
* `activity_favoritos.xml`
* `activity_detalhes.xml`
* `item_favorito.xml`

---

## Requisitos

* Android Studio
* Android SDK (minSdk 24)
* Backend WeatherHub API rodando (porta 8080)
* Chave da OpenWeather configurada via `local.properties`

Config atual do app:

* `minSdk = 24`
* `targetSdk = 36`
* `compileSdk = 36`
* `JavaVersion = 11`

---

## Dependências principais (app/build.gradle.kts)

* Retrofit: `com.squareup.retrofit2:retrofit:2.11.0`
* Gson Converter: `com.squareup.retrofit2:converter-gson:2.11.0`
* OkHttp: `com.squareup.okhttp3:okhttp:4.12.0`
* Logging Interceptor: `com.squareup.okhttp3:logging-interceptor:4.12.0`
* Coil: `io.coil-kt:coil:2.6.0`
* RecyclerView: `androidx.recyclerview:recyclerview:1.3.2`
* Play Services Base: `com.google.android.gms:play-services-base:18.4.0` (ProviderInstaller)

---

## Instalação e execução

### 1) Clonar e abrir

1. Abra o projeto no Android Studio.
2. Aguarde sincronizar o Gradle.

### 2) Configurar a chave da OpenWeather (OBRIGATÓRIO)

No arquivo `local.properties` (na raiz do projeto), adicione:

```properties
OPENWEATHER_API_KEY=SUA_CHAVE_AQUI
```

O app lê isso e expõe via `BuildConfig.OPENWEATHER_API_KEY` (configurado no `app/build.gradle.kts`).

Importante: não commitar essa chave.

### 3) Configurar a URL do backend WeatherHub API (OBRIGATÓRIO)

No arquivo:

`rede/FabricaRetrofit.kt`

ajuste:

```kotlin
private const val URL_BASE_WEATHERHUB = "http://192.168.0.197:8080/"
```

#### Se estiver rodando no EMULADOR

Use:

```text
http://10.0.2.2:8080/
```

`10.0.2.2` é o “localhost do PC” visto pelo emulador.

Se não funcionar use o IP do PC na mesma rede Wi-Fi (ex.: `http://192.168.0.197:8080/`) e garanta que:

* PC e celular estão na mesma rede
* firewall não está bloqueando a porta 8080
* backend está rodando e acessível

### 4) Subir o backend WeatherHub API

O app assume o backend rodando em:

```text
http://<HOST>:8080/
```

### 5) Rodar o app

* Execute no emulador ou dispositivo.
* Na Home, busque uma cidade.

---

## APIs consumidas (Endpoints) 

### A) OpenWeather (externa)

**Base URL:** `https://api.openweathermap.org/`

* **Clima atual**

  * **GET** `data/2.5/weather`
  * **Query:** `q` (cidade), `appid` (chave), `units=metric`, `lang=pt_br`
  * **Usado no app:** `OpenWeatherApi.buscarClimaAtual(...)`
  * **Campos principais usados:** `name`, `sys.country`, `main.temp`, `main.humidity`, `wind.speed`, `coord.lat/lon`, `weather[0].description`, `weather[0].icon`
  * **Model:** `RespostaClima.kt`

* **Previsão 5 dias**

  * **GET** `data/2.5/forecast`
  * **Query:** `q`, `appid`, `units=metric`, `lang=pt_br`
  * **Usado no app:** `OpenWeatherApi.buscarPrevisao5Dias(...)`
  * **Campos principais usados:** `list[].dt_txt`, `list[].main.temp`, `list[].weather[0].description`, `list[].weather[0].icon`
  * **Model:** `RespostaPrevisao.kt`

---

### B) WeatherHub API (backend)

**Base URL:** `http://<HOST>:8080/` (definida em `FabricaRetrofit.kt`)

#### Favoritos

* **GET** `api/users/{userId}/favorites` → lista `ModelFavorito`
* **POST** `api/users/{userId}/favorites` (body `FavoritoCriarRequest`) → retorna `ModelFavorito`
* **DELETE** `api/users/{userId}/favorites/{cityId}` → remove favorito

#### Histórico

* **GET** `api/users/{userId}/history` → lista `ModelHistorico` (no app, Detalhes filtra por cidade)
* **POST** `api/users/{userId}/history` (body `HistoricoCriarRequest`) → retorna `ModelHistorico`
* **DELETE** `api/users/{userId}/history` → limpa histórico inteiro do usuário

**Observação:** o app usa `USER_ID_PADRAO = 1L` e não implementa tela de login/cadastro (endpoints de Users podem existir no backend, mas não são usados aqui).
