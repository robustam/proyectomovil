package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.RutaRepository
import com.arzobispado.redpatrimonial.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Panel del guía voluntario (CU16, CU17, CU18). */
class GuiaViewModel(
    private val repo: RutaRepository = AppContainer.rutaRepository
) : BaseViewModel() {

    private val _guia = MutableStateFlow<Guia?>(null)
    val guia: StateFlow<Guia?> = _guia.asStateFlow()

    private val _asignaciones = MutableStateFlow<List<SalidaPedagogica>>(emptyList())
    val asignaciones: StateFlow<List<SalidaPedagogica>> = _asignaciones.asStateFlow()

    private val _disponibilidad = MutableStateFlow<List<Disponibilidad>>(emptyList())
    val disponibilidad: StateFlow<List<Disponibilidad>> = _disponibilidad.asStateFlow()

    fun cargar(usuario: Usuario) = ejecutar {
        val g = repo.guiaPorUsuario(usuario)
        _guia.value = g
        _asignaciones.value = repo.salidas(idGuia = g.idGuia)
        _disponibilidad.value = repo.disponibilidad(g.idGuia)
    }

    fun cambiarActivo(activo: Boolean) {
        val g = _guia.value ?: return
        ejecutar(mensajeExito = if (activo) "Ahora apareces disponible para guiados" else "Quedaste en pausa") {
            _guia.value = repo.cambiarActivo(g.idGuia, activo)
        }
    }

    /** CU17: aceptar la asignación o pedir reagendar. */
    fun responder(salida: SalidaPedagogica, aceptar: Boolean, comentario: String = "") {
        val g = _guia.value ?: return
        ejecutar(mensajeExito = if (aceptar) "Recorrido aceptado" else "Se informó que necesitas reagendar") {
            repo.responderGuia(salida.idSalida, if (aceptar) EstadosSalida.ACEPTADA else EstadosSalida.REAGENDAR, comentario)
            _asignaciones.value = repo.salidas(idGuia = g.idGuia)
        }
    }

    /** CU18: registrar la finalización del recorrido. */
    fun finalizar(salida: SalidaPedagogica, comentario: String) {
        val g = _guia.value ?: return
        ejecutar(mensajeExito = "Recorrido registrado como finalizado") {
            repo.finalizar(salida.idSalida, comentario)
            _asignaciones.value = repo.salidas(idGuia = g.idGuia)
            _guia.value = _guia.value?.copy(guiadosRealizados = (_guia.value?.guiadosRealizados ?: 0) + 1)
        }
    }

    /** CU16: registrar un bloque de disponibilidad. */
    fun agregarDisponibilidad(fecha: String, inicio: String, fin: String, alGuardar: () -> Unit) {
        val g = _guia.value ?: return
        val error = when {
            !Formatos.esFechaValida(fecha) -> "La fecha debe tener formato AAAA-MM-DD"
            LocalDate.parse(fecha).isBefore(LocalDate.now()) -> "La fecha no puede ser pasada"
            !Formatos.esHoraValida(inicio) || !Formatos.esHoraValida(fin) -> "Las horas deben tener formato HH:MM"
            fin <= inicio -> "La hora de término debe ser posterior al inicio"
            else -> null
        }
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar(mensajeExito = "Disponibilidad registrada", alTerminar = alGuardar) {
            repo.agregarDisponibilidad(g.idGuia, DisponibilidadRequest(fecha, inicio, fin))
            _disponibilidad.value = repo.disponibilidad(g.idGuia)
        }
    }

    fun eliminarDisponibilidad(d: Disponibilidad) = ejecutar(mensajeExito = "Bloque eliminado") {
        repo.eliminarDisponibilidad(d.idDisponibilidad)
        _disponibilidad.value = repo.disponibilidad(d.idGuia)
    }
}
