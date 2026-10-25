package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.RutaRepository
import com.arzobispado.redpatrimonial.util.CalculadoraRuta
import com.arzobispado.redpatrimonial.util.ResultadoRuta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Rutas patrimoniales (CU06, CU09) y el recorrido paso a paso. */
class RutaViewModel(
    private val repo: RutaRepository = AppContainer.rutaRepository
) : BaseViewModel() {

    private val _rutas = MutableStateFlow<List<Ruta>>(emptyList())
    val rutas: StateFlow<List<Ruta>> = _rutas.asStateFlow()

    private val _seleccionada = MutableStateFlow<Ruta?>(null)
    val seleccionada: StateFlow<Ruta?> = _seleccionada.asStateFlow()

    /** Paradas que el usuario va eligiendo al crear su ruta. */
    private val _borrador = MutableStateFlow<List<Parada>>(emptyList())
    val borrador: StateFlow<List<Parada>> = _borrador.asStateFlow()

    private val _calculo = MutableStateFlow(ResultadoRuta(0.0, 0, 0, "Baja"))
    val calculo: StateFlow<ResultadoRuta> = _calculo.asStateFlow()

    /** Recorrido iniciado: índice de la parada actual (-1 = no iniciado). */
    private val _paradaActual = MutableStateFlow(-1)
    val paradaActual: StateFlow<Int> = _paradaActual.asStateFlow()

    fun cargar(idUsuario: Long?) = ejecutar { _rutas.value = repo.rutas(idUsuario) }

    fun cargarDetalle(id: Long) {
        _seleccionada.value = _rutas.value.firstOrNull { it.idRuta == id }
        _paradaActual.value = -1
        ejecutar { _seleccionada.value = repo.ruta(id) }
    }

    // ----- Crear ruta personalizada -----

    fun alternarParada(nodo: NodoPatrimonial) {
        val actual = _borrador.value
        _borrador.value = if (actual.any { it.idNodo == nodo.idNodo }) {
            actual.filter { it.idNodo != nodo.idNodo }
        } else {
            actual + Parada(idNodo = nodo.idNodo, nombreNodo = nodo.nombreOficial, latitud = nodo.latitud, longitud = nodo.longitud)
        }
        recalcular()
    }

    fun cambiarTiempo(idNodo: Long, minutos: Int) {
        _borrador.value = _borrador.value.map { if (it.idNodo == idNodo) it.copy(tiempoSugeridoMin = minutos.coerceIn(5, 240)) else it }
        recalcular()
    }

    fun moverArriba(indice: Int) {
        if (indice <= 0) return
        val lista = _borrador.value.toMutableList()
        val p = lista.removeAt(indice)
        lista.add(indice - 1, p)
        _borrador.value = lista
        recalcular()
    }

    fun limpiarBorrador() {
        _borrador.value = emptyList()
        recalcular()
    }

    private fun recalcular() {
        _calculo.value = CalculadoraRuta.calcular(_borrador.value)
    }

    fun guardarRuta(nombre: String, descripcion: String, enfoque: String, usuario: Usuario?, alGuardar: (Ruta) -> Unit) {
        when {
            usuario == null -> { _mensaje.value = "Inicia sesión para guardar tus rutas"; return }
            nombre.isBlank() -> { _mensaje.value = "Ponle un nombre a tu ruta"; return }
            _borrador.value.size < 2 -> { _mensaje.value = "Elige al menos 2 lugares para la ruta"; return }
        }
        ejecutar(mensajeExito = "Ruta guardada") {
            val ruta = repo.crearRuta(RutaRequest(usuario!!.idUsuario, nombre.trim(), descripcion.trim(), enfoque, _borrador.value))
            _rutas.value = repo.rutas(usuario.idUsuario)
            limpiarBorrador()
            alGuardar(ruta)
        }
    }

    fun eliminarRuta(ruta: Ruta, idUsuario: Long?) = ejecutar(mensajeExito = "Ruta eliminada") {
        repo.eliminarRuta(ruta.idRuta)
        _rutas.value = repo.rutas(idUsuario)
    }

    // ----- Recorrido (simulado, sin GPS) -----

    fun iniciarRecorrido() {
        _paradaActual.value = 0
    }

    fun siguienteParada() {
        val total = _seleccionada.value?.paradas?.size ?: 0
        if (_paradaActual.value < total) _paradaActual.value = _paradaActual.value + 1
        if (_paradaActual.value >= total) _mensaje.value = "¡Recorrido completado! Gracias por visitar la red"
    }

    fun terminarRecorrido() {
        _paradaActual.value = -1
    }
}
