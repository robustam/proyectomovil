package com.arzobispado.redpatrimonial.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Crea un cliente Retrofit por cada microservicio. */
object RetrofitClient {

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(ApiConfig.TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.TIMEOUT_SEGUNDOS * 2, TimeUnit.SECONDS)
            // Muestra las peticiones en el Logcat (útil para la defensa del proyecto)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val usuarioApi: UsuarioApi by lazy { retrofit(ApiConfig.URL_USUARIOS).create(UsuarioApi::class.java) }
    val nodoApi: NodoApi by lazy { retrofit(ApiConfig.URL_NODOS).create(NodoApi::class.java) }
    val eventoApi: EventoApi by lazy { retrofit(ApiConfig.URL_EVENTOS).create(EventoApi::class.java) }
    val rutaApi: RutaApi by lazy { retrofit(ApiConfig.URL_RUTAS).create(RutaApi::class.java) }
    val avisoApi: AvisoApi by lazy { retrofit(ApiConfig.URL_AVISOS).create(AvisoApi::class.java) }
}
