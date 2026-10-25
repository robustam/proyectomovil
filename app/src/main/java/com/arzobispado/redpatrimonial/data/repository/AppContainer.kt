package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.remote.RetrofitClient

/**
 * Contenedor simple de dependencias: crea una sola instancia de cada repositorio
 * y los ViewModels las reciben por constructor.
 */
object AppContainer {
    val authRepository by lazy { AuthRepository(RetrofitClient.usuarioApi) }
    val nodoRepository by lazy { NodoRepository(RetrofitClient.nodoApi) }
    val eventoRepository by lazy { EventoRepository(RetrofitClient.eventoApi) }
    val rutaRepository by lazy { RutaRepository(RetrofitClient.rutaApi) }
    val avisoRepository by lazy { AvisoRepository(RetrofitClient.avisoApi) }
}
