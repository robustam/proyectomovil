package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.EventoRepository
import com.arzobispado.redpatrimonial.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Oferta cultural y litúrgica (CU05, CU12) e inscripciones con QR. */
class EventoViewModel(
    private val repo: EventoRepository = AppContainer.eventoRepository
) : BaseViewModel() {

    private val _eventos = MutableStateFlow<List<Evento>>(emptyList())
    val eventos: StateFlow<List<Evento>> = _eventos.asStateFlow()

    /** Eventos del templo que se está viendo en la ficha. */
    private val _eventosNodo = MutableStateFlow<List<Evento>>(emptyList())
    val eventosNodo: StateFlow<List<Evento>> = _eventosNodo.asStateFlow()

    private val _misInscripciones = MutableStateFlow<List<Inscripcion>>(emptyList())
    val misInscripciones: StateFlow<List<Inscripcion>> = _misInscripciones.asStateFlow()

    /** Ticket que muestra la pantalla de confirmación con QR. */
    private val _ticket = MutableStateFlow<Inscripcion?>(null)
    val ticket: StateFlow<Inscripcion?> = _ticket.asStateFlow()

    init {
        cargar()
    }

    fun cargar() = ejecutar { _eventos.value = repo.listar() }

    fun cargarDeNodo(idNodo: Long) = ejecutar { _eventosNodo.value = repo.listar(idNodo) }

    fun inscribir(evento: Evento, usuario: Usuario, cantidad: Int, alConfirmar: () -> Unit) {
        if (cantidad < 1) {
            _mensaje.value = "Indica cuántas personas asistirán"
            return
        }
        ejecutar(alTerminar = alConfirmar) {
            _ticket.value = repo.inscribir(evento, usuario.idUsuario, cantidad)
            _eventos.value = repo.listar()
            _misInscripciones.value = repo.inscripciones(usuario.idUsuario)
        }
    }

    fun verTicket(inscripcion: Inscripcion) {
        _ticket.value = inscripcion
    }

    fun cargarInscripciones(idUsuario: Long) = ejecutar { _misInscripciones.value = repo.inscripciones(idUsuario) }

    fun cancelarInscripcion(inscripcion: Inscripcion) = ejecutar(mensajeExito = "Inscripción cancelada") {
        repo.cancelarInscripcion(inscripcion.idInscripcion)
        _misInscripciones.value = repo.inscripciones(inscripcion.idUsuario)
        _eventos.value = repo.listar()
    }

    /** CU12: el encargado publica un evento en su templo. */
    fun publicar(
        nodo: NodoPatrimonial, usuario: Usuario, categoria: String, titulo: String, descripcion: String,
        fecha: String, horaInicio: String, horaFin: String, cupo: String, requiereInscripcion: Boolean, alPublicar: () -> Unit
    ) {
        val error = validarEvento(titulo, fecha, horaInicio, horaFin, cupo)
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar(mensajeExito = "Evento publicado en la red", alTerminar = alPublicar) {
            repo.crear(
                EventoRequest(
                    idNodo = nodo.idNodo, nombreNodo = nodo.nombreOficial, latitud = nodo.latitud, longitud = nodo.longitud,
                    categoria = categoria, titulo = titulo.trim(), descripcion = descripcion.trim(),
                    fechaInicio = "${fecha}T$horaInicio", fechaFin = "${fecha}T$horaFin",
                    cupoMaximo = cupo.toIntOrNull() ?: 0, requiereInscripcion = requiereInscripcion, idPublicador = usuario.idUsuario
                )
            )
            _eventos.value = repo.listar()
            _eventosNodo.value = repo.listar(nodo.idNodo)
        }
    }

    fun cancelarEvento(evento: Evento) = ejecutar(mensajeExito = "Evento cancelado") {
        repo.cancelar(evento.idEvento)
        _eventos.value = repo.listar()
        _eventosNodo.value = repo.listar(evento.idNodo)
    }

    companion object {
        fun validarEvento(titulo: String, fecha: String, inicio: String, fin: String, cupo: String): String? = when {
            titulo.isBlank() -> "Ingresa el título del evento"
            !Formatos.esFechaValida(fecha) -> "La fecha debe tener formato AAAA-MM-DD"
            !Formatos.esHoraValida(inicio) || !Formatos.esHoraValida(fin) -> "Las horas deben tener formato HH:MM"
            fin <= inicio -> "La hora de término debe ser posterior al inicio"
            cupo.isNotBlank() && (cupo.toIntOrNull() ?: -1) < 0 -> "El cupo debe ser un número"
            else -> null
        }
    }
}
