package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.remote.NodoApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Repositorio de fichas patrimoniales, horarios y galería (ms-nodos). */
class NodoRepository(private val api: NodoApi) {

    suspend fun listar(): List<NodoPatrimonial> = conRespaldo(
        remoto = { api.listar() },
        local = { DatosPrueba.nodos.map { conEstadoActual(it) } }
    )

    suspend fun buscar(id: Long): NodoPatrimonial = conRespaldo(
        remoto = { api.buscar(id) },
        local = { conEstadoActual(local(id)) }
    )

    /** CU04/CU11: el encargado cambia el estado de su templo. */
    suspend fun cambiarEstado(id: Long, estado: String): NodoPatrimonial = conRespaldo(
        remoto = { api.cambiarEstado(id, EstadoRequest(estado)) },
        local = { guardar(local(id).copy(estadoOperativo = estado)) }
    )

    /** CU13: modificar ficha. */
    suspend fun actualizar(id: Long, req: NodoRequest): NodoPatrimonial = conRespaldo(
        remoto = { api.actualizar(id, req) },
        local = {
            guardar(local(id).copy(
                nombreOficial = req.nombreOficial, direccion = req.direccion,
                resenaHistorica = req.resenaHistorica, estiloArquitectonico = req.estiloArquitectonico,
                anioConstruccion = req.anioConstruccion
            ))
        }
    )

    /** CU11: horario excepcional (misma validación que el backend). */
    suspend fun agregarHorario(idNodo: Long, req: HorarioExcepcionalRequest): HorarioExcepcional = conRespaldo(
        remoto = { api.agregarHorario(idNodo, req) },
        local = {
            if (!LocalTime.parse(req.horaFin).isAfter(LocalTime.parse(req.horaInicio))) {
                throw ErrorApi("La hora de término debe ser posterior a la de inicio")
            }
            if (LocalDate.parse(req.fecha).isBefore(LocalDate.now())) {
                throw ErrorApi("No se pueden registrar horarios en fechas pasadas")
            }
            val nodo = local(idNodo)
            val nuevo = HorarioExcepcional(DatosPrueba.nuevoId(), idNodo, req.fecha, req.horaInicio, req.horaFin, req.estadoApertura, req.motivo)
            guardar(nodo.copy(horariosExcepcionales = (nodo.horariosExcepcionales + nuevo).sortedBy { it.fecha + it.horaInicio }))
            nuevo
        }
    )

    suspend fun eliminarHorario(idNodo: Long, idHorario: Long) = conRespaldo(
        remoto = { api.eliminarHorario(idHorario) },
        local = {
            val nodo = local(idNodo)
            guardar(nodo.copy(horariosExcepcionales = nodo.horariosExcepcionales.filter { it.idHorarioExc != idHorario }))
            Unit
        }
    )

    /** CU13: galería multimedia simulada. */
    suspend fun agregarRecurso(idNodo: Long, req: RecursoRequest): RecursoMultimedia = conRespaldo(
        remoto = { api.agregarRecurso(idNodo, req) },
        local = {
            val nodo = local(idNodo)
            val nuevo = RecursoMultimedia(DatosPrueba.nuevoId(), req.tipoRecurso, req.titulo,
                req.urlRecurso.ifBlank { "https://ejemplo.cl/recursos/simulado.jpg" }, req.duracionSegundos, req.esPrincipal)
            guardar(nodo.copy(recursos = nodo.recursos + nuevo))
            nuevo
        }
    )

    suspend fun eliminarRecurso(idNodo: Long, idRecurso: Long) = conRespaldo(
        remoto = { api.eliminarRecurso(idRecurso) },
        local = {
            val nodo = local(idNodo)
            guardar(nodo.copy(recursos = nodo.recursos.filter { it.idRecurso != idRecurso }))
            Unit
        }
    )

    // ----- ayudas para el modo sin conexión -----

    private fun local(id: Long): NodoPatrimonial =
        DatosPrueba.nodos.firstOrNull { it.idNodo == id } ?: throw ErrorApi("Nodo $id no existe")

    private fun guardar(nodo: NodoPatrimonial): NodoPatrimonial {
        val i = DatosPrueba.nodos.indexOfFirst { it.idNodo == nodo.idNodo }
        DatosPrueba.nodos[i] = nodo
        return conEstadoActual(nodo)
    }

    /** Igual que ms-nodos: un horario excepcional vigente ahora manda sobre el estado configurado. */
    private fun conEstadoActual(nodo: NodoPatrimonial): NodoPatrimonial {
        val ahora = LocalDateTime.now()
        val hoy = ahora.toLocalDate().toString()
        val hora = ahora.toLocalTime()
        val excepcional = nodo.horariosExcepcionales.firstOrNull {
            it.fecha == hoy && !hora.isBefore(LocalTime.parse(it.horaInicio)) && hora.isBefore(LocalTime.parse(it.horaFin))
        }
        return nodo.copy(
            estadoActual = excepcional?.estadoApertura ?: nodo.estadoOperativo,
            horariosExcepcionales = nodo.horariosExcepcionales.filter { it.fecha >= hoy }
        )
    }
}
