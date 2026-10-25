package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.remote.EventoApi
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

/** Repositorio de oferta cultural e inscripciones (ms-eventos). */
class EventoRepository(private val api: EventoApi) {

    suspend fun listar(idNodo: Long? = null): List<Evento> = conRespaldo(
        remoto = { api.listar(idNodo) },
        local = {
            val hoy = LocalDate.now().toString()
            DatosPrueba.eventos
                .filter { idNodo == null || it.idNodo == idNodo }
                .filter { it.fechaFin.take(10) >= hoy }
                .sortedBy { it.fechaInicio }
        }
    )

    /** CU12: publicar evento. */
    suspend fun crear(req: EventoRequest): Evento = conRespaldo(
        remoto = { api.crear(req) },
        local = {
            if (LocalDateTime.parse(req.fechaFin) <= LocalDateTime.parse(req.fechaInicio)) {
                throw ErrorApi("La fecha de término debe ser posterior al inicio")
            }
            val nuevo = Evento(DatosPrueba.nuevoId(), req.idNodo, req.nombreNodo, req.latitud, req.longitud, req.categoria,
                req.titulo, req.descripcion, req.fechaInicio, req.fechaFin, req.cupoMaximo, req.cupoMaximo,
                req.requiereInscripcion, "PROGRAMADO")
            DatosPrueba.eventos.add(nuevo)
            nuevo
        }
    )

    suspend fun cancelar(idEvento: Long): Evento = conRespaldo(
        remoto = { api.cancelar(idEvento) },
        local = { reemplazarEvento(idEvento) { it.copy(estado = "CANCELADO") } }
    )

    /** Reserva de cupo con QR (mismas reglas que el backend). */
    suspend fun inscribir(evento: Evento, idUsuario: Long, cantidad: Int): Inscripcion = conRespaldo(
        remoto = { api.inscribir(evento.idEvento, InscripcionRequest(idUsuario, cantidad)) },
        local = {
            val actual = DatosPrueba.eventos.first { it.idEvento == evento.idEvento }
            if (actual.estado != "PROGRAMADO") throw ErrorApi("El evento no está disponible (${actual.estado})")
            if (DatosPrueba.inscripciones.any { it.idEvento == actual.idEvento && it.idUsuario == idUsuario && it.estadoAsistencia == "CONFIRMADA" }) {
                throw ErrorApi("Ya tienes una inscripción activa para este evento")
            }
            if (actual.cupoMaximo > 0 && cantidad > actual.cuposDisponibles) {
                throw ErrorApi("No hay cupos suficientes. Disponibles: ${actual.cuposDisponibles}")
            }
            if (actual.cupoMaximo > 0) reemplazarEvento(actual.idEvento) { it.copy(cuposDisponibles = it.cuposDisponibles - cantidad) }
            val ticket = Inscripcion(
                DatosPrueba.nuevoId(), actual.idEvento, idUsuario, actual.titulo, actual.nombreNodo, actual.fechaInicio,
                cantidad, LocalDateTime.now().toString(), "CONFIRMADA",
                "PAS-${LocalDate.now().year}-${Random.nextInt(10000, 99999)}"
            )
            DatosPrueba.inscripciones.add(0, ticket)
            ticket
        }
    )

    suspend fun inscripciones(idUsuario: Long): List<Inscripcion> = conRespaldo(
        remoto = { api.inscripciones(idUsuario) },
        local = { DatosPrueba.inscripciones.filter { it.idUsuario == idUsuario } }
    )

    suspend fun cancelarInscripcion(id: Long): Inscripcion = conRespaldo(
        remoto = { api.cancelarInscripcion(id) },
        local = {
            val i = DatosPrueba.inscripciones.indexOfFirst { it.idInscripcion == id }
            if (i < 0) throw ErrorApi("Inscripción no existe")
            val cancelada = DatosPrueba.inscripciones[i].copy(estadoAsistencia = "CANCELADA")
            DatosPrueba.inscripciones[i] = cancelada
            cancelada
        }
    )

    private fun reemplazarEvento(id: Long, cambio: (Evento) -> Evento): Evento {
        val i = DatosPrueba.eventos.indexOfFirst { it.idEvento == id }
        if (i < 0) throw ErrorApi("Evento $id no existe")
        DatosPrueba.eventos[i] = cambio(DatosPrueba.eventos[i])
        return DatosPrueba.eventos[i]
    }
}
