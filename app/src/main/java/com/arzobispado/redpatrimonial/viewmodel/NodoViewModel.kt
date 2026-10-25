package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.NodoRepository
import com.arzobispado.redpatrimonial.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Filtros de la pantalla Explorar (CU02). */
data class FiltrosNodo(
    val texto: String = "",
    val tipo: String? = null,
    val comuna: String? = null,
    val soloAbiertos: Boolean = false
)

/**
 * Fichas patrimoniales: mapa, búsqueda con filtros, detalle, favoritos
 * y las acciones del encargado de templo.
 */
class NodoViewModel(
    private val repo: NodoRepository = AppContainer.nodoRepository
) : BaseViewModel() {

    private val _nodos = MutableStateFlow<List<NodoPatrimonial>>(emptyList())
    val nodos: StateFlow<List<NodoPatrimonial>> = _nodos.asStateFlow()

    private val _filtros = MutableStateFlow(FiltrosNodo())
    val filtros: StateFlow<FiltrosNodo> = _filtros.asStateFlow()

    /** Lista filtrada: se recalcula cada vez que cambian los nodos o los filtros. */
    private val _nodosFiltrados = MutableStateFlow<List<NodoPatrimonial>>(emptyList())
    val nodosFiltrados: StateFlow<List<NodoPatrimonial>> = _nodosFiltrados.asStateFlow()

    private val _comunas = MutableStateFlow<List<String>>(emptyList())
    val comunas: StateFlow<List<String>> = _comunas.asStateFlow()

    /** Guarda la lista de nodos y actualiza las listas que dependen de ella. */
    private fun ponerNodos(lista: List<NodoPatrimonial>) {
        _nodos.value = lista
        _comunas.value = lista.map { it.comuna }.distinct().sorted()
        _nodosFiltrados.value = aplicarFiltros(lista, _filtros.value)
    }

    private val _seleccionado = MutableStateFlow<NodoPatrimonial?>(null)
    val seleccionado: StateFlow<NodoPatrimonial?> = _seleccionado.asStateFlow()

    /** Favoritos de la sesión (se guardarán con Room cuando se vea en clases). */
    private val _favoritos = MutableStateFlow<Set<Long>>(emptySet())
    val favoritos: StateFlow<Set<Long>> = _favoritos.asStateFlow()

    init {
        cargar()
    }

    fun cargar() = ejecutar { ponerNodos(repo.listar()) }

    fun cargarDetalle(id: Long) {
        _seleccionado.value = _nodos.value.firstOrNull { it.idNodo == id }
        ejecutar { _seleccionado.value = repo.buscar(id) }
    }

    fun nodoPorId(id: Long?): NodoPatrimonial? = _nodos.value.firstOrNull { it.idNodo == id }

    fun actualizarFiltros(f: FiltrosNodo) {
        _filtros.value = f
        _nodosFiltrados.value = aplicarFiltros(_nodos.value, f)
    }

    fun alternarFavorito(id: Long) {
        _favoritos.value = if (id in _favoritos.value) _favoritos.value - id else _favoritos.value + id
    }

    // ----- Acciones del encargado de templo -----

    fun cambiarEstado(idNodo: Long, estado: String) =
        ejecutar(mensajeExito = "Estado actualizado: ${EstadosApertura.nombreVisible(estado)}") {
            refrescar(repo.cambiarEstado(idNodo, estado))
        }

    fun agregarHorario(idNodo: Long, fecha: String, inicio: String, fin: String, estado: String, motivo: String, alGuardar: () -> Unit) {
        val error = when {
            !Formatos.esFechaValida(fecha) -> "La fecha debe tener formato AAAA-MM-DD"
            LocalDate.parse(fecha).isBefore(LocalDate.now()) -> "No se pueden registrar horarios en fechas pasadas"
            !Formatos.esHoraValida(inicio) || !Formatos.esHoraValida(fin) -> "Las horas deben tener formato HH:MM"
            fin <= inicio -> "La hora de término debe ser posterior a la de inicio"
            else -> null
        }
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar(mensajeExito = "Horario excepcional publicado", alTerminar = alGuardar) {
            repo.agregarHorario(idNodo, HorarioExcepcionalRequest(fecha, inicio, fin, estado, motivo.trim()))
            refrescar(repo.buscar(idNodo))
        }
    }

    fun eliminarHorario(idNodo: Long, idHorario: Long) = ejecutar(mensajeExito = "Horario eliminado") {
        repo.eliminarHorario(idNodo, idHorario)
        refrescar(repo.buscar(idNodo))
    }

    fun actualizarFicha(nodo: NodoPatrimonial, nombre: String, direccion: String, resena: String, estilo: String, anio: Int?, alGuardar: () -> Unit) =
        ejecutar(mensajeExito = "Ficha actualizada", alTerminar = alGuardar) {
            val req = NodoRequest(nombre, direccion, nodo.comuna, nodo.tipoNodo, nodo.latitud, nodo.longitud, resena, estilo, anio)
            refrescar(repo.actualizar(nodo.idNodo, req))
        }

    fun agregarRecurso(idNodo: Long, tipo: String, titulo: String, url: String) = ejecutar(mensajeExito = "Recurso agregado a la galería") {
        repo.agregarRecurso(idNodo, RecursoRequest(tipo, titulo, url))
        refrescar(repo.buscar(idNodo))
    }

    fun eliminarRecurso(idNodo: Long, idRecurso: Long) = ejecutar(mensajeExito = "Recurso eliminado") {
        repo.eliminarRecurso(idNodo, idRecurso)
        refrescar(repo.buscar(idNodo))
    }

    /** Reemplaza el nodo actualizado en la lista y en el detalle. */
    private fun refrescar(nodo: NodoPatrimonial) {
        ponerNodos(_nodos.value.map { if (it.idNodo == nodo.idNodo) nodo else it })
        if (_seleccionado.value?.idNodo == nodo.idNodo) _seleccionado.value = nodo
    }

    companion object {
        fun aplicarFiltros(lista: List<NodoPatrimonial>, f: FiltrosNodo): List<NodoPatrimonial> = lista
            .filter {
                f.texto.isBlank() || it.nombreOficial.contains(f.texto, true) ||
                    it.direccion.contains(f.texto, true) || it.comuna.contains(f.texto, true) ||
                    it.estiloArquitectonico.contains(f.texto, true)
            }
            .filter { f.tipo == null || it.tipoNodo == f.tipo }
            .filter { f.comuna == null || it.comuna == f.comuna }
            .filter { !f.soloAbiertos || it.estadoActual == EstadosApertura.ABIERTO }
            .sortedBy { it.nombreOficial }
    }
}
