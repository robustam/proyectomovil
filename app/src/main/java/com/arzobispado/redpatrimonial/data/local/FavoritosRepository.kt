package com.arzobispado.redpatrimonial.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de favoritos: es la única puerta entre los ViewModels y Room.
 *
 * - Con base de datos (dao != null): los favoritos quedan guardados en el teléfono.
 * - Sin base de datos (dao == null, por ejemplo en las vistas previas de Android Studio):
 *   usa una lista en memoria para que la pantalla siga funcionando.
 */
class FavoritosRepository(private val dao: FavoritoDao?) {

    /** Respaldo en memoria, solo se usa cuando no hay base de datos. */
    private val memoria = MutableStateFlow<Set<Long>>(emptySet())

    /**
     * Entrega los ids favoritos como un flujo que se actualiza solo.
     * "map" convierte la lista de Room en un Set (búsqueda rápida con "in").
     */
    fun observar(): Flow<Set<Long>> =
        dao?.observarIds()?.map { it.toSet() } ?: memoria

    /** Marca o desmarca un nodo: si ya está guardado lo borra, si no lo guarda. */
    suspend fun alternar(idNodo: Long) {
        if (dao == null) {
            // Sin base: se cambia la lista en memoria
            memoria.value = if (idNodo in memoria.value) memoria.value - idNodo else memoria.value + idNodo
        } else if (dao.existe(idNodo)) {
            // Ya era favorito -> se quita
            dao.borrar(idNodo)
        } else {
            // No era favorito -> se agrega
            dao.guardar(FavoritoEntity(idNodo))
        }
    }
}
