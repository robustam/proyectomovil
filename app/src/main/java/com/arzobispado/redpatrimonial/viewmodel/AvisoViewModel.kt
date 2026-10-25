package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.AvisoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Avisos de la red en vivo (CU14), al estilo de una plataforma comunitaria. */
class AvisoViewModel(
    private val repo: AvisoRepository = AppContainer.avisoRepository
) : BaseViewModel() {

    private val _avisos = MutableStateFlow<List<Aviso>>(emptyList())
    val avisos: StateFlow<List<Aviso>> = _avisos.asStateFlow()

    /** null = todas las categorías */
    private val _categoria = MutableStateFlow<String?>(null)
    val categoria: StateFlow<String?> = _categoria.asStateFlow()

    private val _avisosFiltrados = MutableStateFlow<List<Aviso>>(emptyList())
    val avisosFiltrados: StateFlow<List<Aviso>> = _avisosFiltrados.asStateFlow()

    /** Guarda los avisos y recalcula la lista filtrada. */
    private fun ponerAvisos(lista: List<Aviso>) {
        _avisos.value = lista
        _avisosFiltrados.value = filtrarPorCategoria(lista, _categoria.value)
    }

    private fun filtrarPorCategoria(lista: List<Aviso>, c: String?) =
        if (c == null) lista else lista.filter { it.categoria == c }

    init {
        cargar()
    }

    fun cargar() = ejecutar { ponerAvisos(repo.listar()) }

    fun filtrar(categoria: String?) {
        _categoria.value = categoria
        _avisosFiltrados.value = filtrarPorCategoria(_avisos.value, categoria)
    }

    fun publicar(
        nodo: NodoPatrimonial, usuario: Usuario, categoria: String, titulo: String, mensaje: String,
        horasVigencia: Int, alPublicar: () -> Unit
    ) {
        val error = when {
            titulo.isBlank() -> "Ingresa el título del aviso"
            titulo.length > 100 -> "El título no puede superar 100 caracteres"
            mensaje.isBlank() -> "Describe el aviso"
            else -> null
        }
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar(mensajeExito = "Aviso publicado: ya es visible en toda la red", alTerminar = alPublicar) {
            repo.publicar(
                AvisoRequest(nodo.idNodo, nodo.nombreOficial, usuario.idUsuario, categoria, titulo.trim(),
                    mensaje.trim(), horasVigencia, nodo.latitud, nodo.longitud)
            )
            ponerAvisos(repo.listar())
        }
    }

    fun retirar(aviso: Aviso) = ejecutar(mensajeExito = "Aviso retirado") {
        repo.retirar(aviso.idAviso)
        ponerAvisos(repo.listar())
    }

    fun reportar(aviso: Aviso, usuario: Usuario, motivo: String) = ejecutar(mensajeExito = "Gracias, revisaremos el aviso") {
        repo.reportar(aviso.idAviso, usuario.idUsuario, motivo)
    }
}
