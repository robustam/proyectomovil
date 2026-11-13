package com.arzobispado.redpatrimonial.data.repository

import android.content.Context
import com.arzobispado.redpatrimonial.data.local.AppDatabase
import com.arzobispado.redpatrimonial.data.local.FavoritosRepository
import com.arzobispado.redpatrimonial.data.remote.RetrofitClient

/**
 * Contenedor simple de dependencias (inyección manual).
 * Crea una sola instancia de cada repositorio y los ViewModels la reciben por constructor.
 * "object" = existe una única copia en toda la app (patrón singleton).
 */
object AppContainer {
    /** Base Room; empieza en null hasta que se llama a iniciar(). */
    private var base: AppDatabase? = null

    /**
     * Crea la base Room. Se llama al abrir la app (en AppNavigation).
     * El "if (base == null)" evita crearla dos veces si la pantalla se recompone.
     */
    fun iniciar(contexto: Context) {
        if (base == null) base = AppDatabase.crear(contexto)
    }

    // "by lazy" = el repositorio se crea la primera vez que se usa, no antes.
    val authRepository by lazy { AuthRepository(RetrofitClient.usuarioApi) }
    val nodoRepository by lazy { NodoRepository(RetrofitClient.nodoApi) }
    val eventoRepository by lazy { EventoRepository(RetrofitClient.eventoApi) }
    val rutaRepository by lazy { RutaRepository(RetrofitClient.rutaApi) }
    val avisoRepository by lazy { AvisoRepository(RetrofitClient.avisoApi) }

    /** Favoritos: usa Room si ya se llamó a iniciar(); si no, queda en memoria. */
    val favoritosRepository: FavoritosRepository by lazy { FavoritosRepository(base?.favoritoDao()) }
}
