package cl.arzobispado.redpatrimonial.eventos.service

import cl.arzobispado.redpatrimonial.eventos.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.eventos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.eventos.dto.*
import cl.arzobispado.redpatrimonial.eventos.model.EventoCultural
import cl.arzobispado.redpatrimonial.eventos.model.InscripcionEvento
import cl.arzobispado.redpatrimonial.eventos.repository.CategoriaEventoRepository
import cl.arzobispado.redpatrimonial.eventos.repository.EventoRepository
import cl.arzobispado.redpatrimonial.eventos.repository.InscripcionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.random.Random

@Service
class EventoService(
    private val eventoRepository: EventoRepository,
    private val categoriaRepository: CategoriaEventoRepository,
    private val inscripcionRepository: InscripcionRepository,
    private val nodosClient: NodosClient
) {

    /** CU05 - Oferta programática. Por defecto muestra solo eventos desde hoy. */
    @Transactional(readOnly = true)
    fun listar(idNodo: Long?, categoria: String?, incluirPasados: Boolean): List<EventoResponse> {
        val hoy = LocalDate.now().atStartOfDay()
        return eventoRepository.findAllByOrderByFechaInicioAsc()
            .filter { idNodo == null || it.idNodo == idNodo }
            .filter { categoria.isNullOrBlank() || it.categoria?.nombreCategoria.equals(categoria, true) }
            .filter { incluirPasados || !it.fechaFin.isBefore(hoy) }
            .map { aRespuesta(it) }
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): EventoResponse = aRespuesta(obtener(id))

    /** CU12 - El encargado publica un evento en su templo. */
    @Transactional
    fun crear(req: EventoRequest): EventoResponse {
        val evento = EventoCultural()
        aplicar(evento, req)
        return aRespuesta(eventoRepository.save(evento))
    }

    @Transactional
    fun actualizar(id: Long, req: EventoRequest): EventoResponse {
        val evento = obtener(id)
        aplicar(evento, req)
        return aRespuesta(eventoRepository.save(evento))
    }

    @Transactional
    fun cancelar(id: Long): EventoResponse {
        val evento = obtener(id)
        evento.estado = "CANCELADO"
        return aRespuesta(eventoRepository.save(evento))
    }

    @Transactional(readOnly = true)
    fun categorias(): List<CategoriaDto> =
        categoriaRepository.findAll().map { CategoriaDto(it.idCategoriaEv!!, it.nombreCategoria, it.descripcion) }

    /** Reserva de cupo con código QR. */
    @Transactional
    fun inscribir(idEvento: Long, req: InscripcionRequest): InscripcionResponse {
        val evento = obtener(idEvento)
        if (evento.estado != "PROGRAMADO") throw ReglaNegocioException("El evento no está disponible (${evento.estado})")
        if (evento.fechaFin.isBefore(LocalDateTime.now())) throw ReglaNegocioException("El evento ya terminó")
        val activas = inscripcionRepository.findByEventoIdEventoAndIdUsuarioAndEstadoAsistencia(idEvento, req.idUsuario, "CONFIRMADA")
        if (activas.isNotEmpty()) throw ReglaNegocioException("Ya tienes una inscripción activa para este evento")
        if (evento.cupoMaximo > 0) {
            val disponibles = evento.cupoMaximo - inscripcionRepository.personasInscritas(idEvento).toInt()
            if (req.cantidadPersonas > disponibles) {
                throw ReglaNegocioException("No hay cupos suficientes. Disponibles: $disponibles")
            }
        }
        val inscripcion = InscripcionEvento(
            evento = evento,
            idUsuario = req.idUsuario,
            cantidadPersonas = req.cantidadPersonas,
            codigoQr = generarCodigo()
        )
        return aTicket(inscripcionRepository.save(inscripcion))
    }

    @Transactional(readOnly = true)
    fun inscripcionesDe(idUsuario: Long): List<InscripcionResponse> =
        inscripcionRepository.findByIdUsuarioOrderByFechaInscripcionDesc(idUsuario).map { aTicket(it) }

    @Transactional
    fun cancelarInscripcion(id: Long): InscripcionResponse {
        val inscripcion = inscripcionRepository.findById(id)
            .orElseThrow { NoEncontradoException("Inscripción $id no existe") }
        inscripcion.estadoAsistencia = "CANCELADA"
        return aTicket(inscripcionRepository.save(inscripcion))
    }

    /** Validación del QR en la entrada del recinto: marca la asistencia. */
    @Transactional
    fun validarCodigo(codigo: String): InscripcionResponse {
        val inscripcion = inscripcionRepository.findByCodigoQr(codigo)
            ?: throw NoEncontradoException("Código $codigo no válido")
        if (inscripcion.estadoAsistencia == "CANCELADA") throw ReglaNegocioException("La inscripción fue cancelada")
        inscripcion.estadoAsistencia = "ASISTIO"
        return aTicket(inscripcionRepository.save(inscripcion))
    }

    /** Código único tipo PAS-2026-48213. */
    fun generarCodigo(): String {
        var codigo: String
        do {
            codigo = "PAS-${LocalDate.now().year}-${Random.nextInt(10000, 99999)}"
        } while (inscripcionRepository.findByCodigoQr(codigo) != null)
        return codigo
    }

    private fun obtener(id: Long) =
        eventoRepository.findById(id).orElseThrow { NoEncontradoException("Evento $id no existe") }

    private fun aplicar(evento: EventoCultural, req: EventoRequest) {
        val inicio = parseFechaHora(req.fechaInicio)
        val fin = parseFechaHora(req.fechaFin)
        if (!fin.isAfter(inicio)) throw ReglaNegocioException("La fecha de término debe ser posterior al inicio")
        val categoria = categoriaRepository.findByNombreCategoriaIgnoreCase(req.categoria)
            ?: throw ReglaNegocioException("Categoría ${req.categoria} no existe")

        // Si la app no envía nombre/ubicación del nodo, se piden a ms-nodos
        val nodo = if (req.nombreNodo.isBlank() || req.latitud == null) nodosClient.buscar(req.idNodo) else null

        evento.idNodo = req.idNodo
        evento.nombreNodo = req.nombreNodo.ifBlank { nodo?.nombreOficial ?: "Nodo ${req.idNodo}" }
        evento.latitud = req.latitud ?: nodo?.latitud
        evento.longitud = req.longitud ?: nodo?.longitud
        evento.categoria = categoria
        evento.titulo = req.titulo.trim()
        evento.descripcion = req.descripcion
        evento.fechaInicio = inicio
        evento.fechaFin = fin
        evento.cupoMaximo = req.cupoMaximo
        evento.requiereInscripcion = req.requiereInscripcion
        evento.idPublicador = req.idPublicador
    }

    private fun parseFechaHora(valor: String): LocalDateTime =
        try { LocalDateTime.parse(valor.trim()) }
        catch (e: DateTimeParseException) { throw ReglaNegocioException("Fecha inválida: $valor (use AAAA-MM-DDTHH:MM)") }

    private fun aRespuesta(e: EventoCultural): EventoResponse {
        val inscritos = if (e.cupoMaximo > 0) inscripcionRepository.personasInscritas(e.idEvento!!).toInt() else 0
        return EventoResponse(
            idEvento = e.idEvento!!,
            idNodo = e.idNodo,
            nombreNodo = e.nombreNodo,
            latitud = e.latitud,
            longitud = e.longitud,
            categoria = e.categoria?.nombreCategoria ?: "",
            titulo = e.titulo,
            descripcion = e.descripcion,
            fechaInicio = e.fechaInicio.toString(),
            fechaFin = e.fechaFin.toString(),
            cupoMaximo = e.cupoMaximo,
            cuposDisponibles = if (e.cupoMaximo > 0) (e.cupoMaximo - inscritos).coerceAtLeast(0) else 0,
            requiereInscripcion = e.requiereInscripcion,
            estado = e.estado
        )
    }

    private fun aTicket(i: InscripcionEvento) = InscripcionResponse(
        idInscripcion = i.idInscripcion!!,
        idEvento = i.evento.idEvento!!,
        idUsuario = i.idUsuario,
        tituloEvento = i.evento.titulo,
        nombreNodo = i.evento.nombreNodo,
        fechaEvento = i.evento.fechaInicio.toString(),
        cantidadPersonas = i.cantidadPersonas,
        fechaInscripcion = i.fechaInscripcion.toString(),
        estadoAsistencia = i.estadoAsistencia,
        codigoQr = i.codigoQr
    )
}
