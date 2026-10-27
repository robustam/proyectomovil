package cl.arzobispado.redpatrimonial.rutas.service

import cl.arzobispado.redpatrimonial.rutas.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.rutas.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.rutas.dto.*
import cl.arzobispado.redpatrimonial.rutas.model.AsignacionGuia
import cl.arzobispado.redpatrimonial.rutas.model.EvaluacionSalida
import cl.arzobispado.redpatrimonial.rutas.model.SalidaPedagogica
import cl.arzobispado.redpatrimonial.rutas.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Service
class SalidaService(
    private val salidaRepository: SalidaRepository,
    private val rutaRepository: RutaRepository,
    private val objetivoRepository: ObjetivoRepository,
    private val asignacionRepository: AsignacionRepository,
    private val evaluacionRepository: EvaluacionRepository,
    private val guiaService: GuiaService,
    private val guiaRepository: GuiaRepository
) {

    @Transactional(readOnly = true)
    fun objetivos(): List<ObjetivoDto> =
        objetivoRepository.findAll().map { ObjetivoDto(it.idObjetivo!!, it.codigoCurricular, it.descripcion, it.asignatura) }

    /** CU07 + CU08 - El profesor planifica la salida y (opcionalmente) solicita un guía. */
    @Transactional
    fun crear(req: SalidaRequest): SalidaResponse {
        val ruta = rutaRepository.findById(req.idRuta).orElseThrow { NoEncontradoException("Ruta ${req.idRuta} no existe") }
        val fecha = try { LocalDate.parse(req.fechaProgramada) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Fecha inválida") }
        val hora = try { LocalTime.parse(req.horaInicio) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Hora inválida") }
        if (fecha.isBefore(LocalDate.now())) throw ReglaNegocioException("La salida debe programarse para hoy o una fecha futura")

        val salida = salidaRepository.save(
            SalidaPedagogica(
                idProfesor = req.idProfesor, nombreProfesor = req.nombreProfesor, colegio = req.colegio.trim(),
                curso = req.curso.trim(), cantidadAlumnos = req.cantidadAlumnos, ruta = ruta,
                fechaProgramada = fecha, horaInicio = hora, notasGuia = req.notasGuia,
                objetivos = objetivoRepository.findAllById(req.idObjetivos).toMutableSet()
            )
        )
        req.idGuia?.let { idGuia ->
            asignacionRepository.save(AsignacionGuia(guia = guiaService.obtener(idGuia), salida = salida))
        }
        return aRespuesta(salida)
    }

    /** Filtros: ?idProfesor= (mis salidas), ?idGuia= (recorridos asignados), ?idNodo= (solicitudes de un templo). */
    @Transactional(readOnly = true)
    fun listar(idProfesor: Long?, idGuia: Long?, idNodo: Long?, estado: String?): List<SalidaResponse> {
        val salidas = if (idGuia != null) {
            asignacionRepository.findByGuiaIdGuia(idGuia).mapNotNull { it.salida }
        } else {
            salidaRepository.findAll()
        }
        return salidas
            .filter { idProfesor == null || it.idProfesor == idProfesor }
            .filter { idNodo == null || (it.ruta?.paradas?.any { p -> p.idNodo == idNodo } ?: false) }
            .filter { estado.isNullOrBlank() || it.estadoSalida.equals(estado, true) }
            .sortedWith(compareBy<SalidaPedagogica> { it.fechaProgramada }.thenBy { it.horaInicio })
            .map { aRespuesta(it) }
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): SalidaResponse = aRespuesta(obtener(id))

    /** CU10 / CU15 - El encargado del templo confirma o rechaza la visita grupal. */
    @Transactional
    fun cambiarEstado(id: Long, req: CambioEstadoRequest): SalidaResponse {
        val salida = obtener(id)
        val nuevo = req.estado.uppercase()
        if (nuevo !in setOf("CONFIRMADA", "RECHAZADA")) throw ReglaNegocioException("Estado inválido: $nuevo")
        if (salida.estadoSalida == "FINALIZADA") throw ReglaNegocioException("La salida ya fue finalizada")
        salida.estadoSalida = nuevo
        return aRespuesta(salidaRepository.save(salida))
    }

    /** CU17 - El guía acepta la asignación o pide reagendar. */
    @Transactional
    fun responderGuia(id: Long, req: CambioEstadoRequest): SalidaResponse {
        val asignacion = asignacionRepository.findBySalidaIdSalida(id)
            ?: throw ReglaNegocioException("La salida no tiene guía asignado")
        val nuevo = req.estado.uppercase()
        if (nuevo !in setOf("ACEPTADA", "REAGENDAR")) throw ReglaNegocioException("Respuesta inválida: $nuevo")
        if (asignacion.estadoConfirmacion == "FINALIZADA") throw ReglaNegocioException("El recorrido ya fue finalizado")
        asignacion.estadoConfirmacion = nuevo
        asignacion.comentarioGuia = req.comentario
        asignacionRepository.save(asignacion)
        return aRespuesta(obtener(id))
    }

    /** Reasignar guía (por ejemplo después de un "reagendar"). */
    @Transactional
    fun asignarGuia(id: Long, idGuia: Long): SalidaResponse {
        val salida = obtener(id)
        val guia = guiaService.obtener(idGuia)
        val asignacion = asignacionRepository.findBySalidaIdSalida(id) ?: AsignacionGuia(salida = salida)
        asignacion.guia = guia
        asignacion.estadoConfirmacion = "PENDIENTE"
        asignacion.comentarioGuia = ""
        asignacionRepository.save(asignacion)
        return aRespuesta(salida)
    }

    /** CU18 - El guía registra la finalización del recorrido. */
    @Transactional
    fun finalizar(id: Long, req: CambioEstadoRequest): SalidaResponse {
        val salida = obtener(id)
        val asignacion = asignacionRepository.findBySalidaIdSalida(id)
            ?: throw ReglaNegocioException("La salida no tiene guía asignado")
        if (asignacion.estadoConfirmacion != "ACEPTADA") {
            throw ReglaNegocioException("Solo se pueden finalizar recorridos aceptados por el guía")
        }
        asignacion.estadoConfirmacion = "FINALIZADA"
        asignacionRepository.save(asignacion)
        asignacion.guia?.let {
            it.guiadosRealizados += 1
            guiaRepository.save(it)
        }
        salida.estadoSalida = "FINALIZADA"
        salida.comentarioFinal = req.comentario
        return aRespuesta(salidaRepository.save(salida))
    }

    /** El docente evalúa la salida; se actualiza el promedio del guía. */
    @Transactional
    fun evaluar(id: Long, req: EvaluacionRequest): SalidaResponse {
        val salida = obtener(id)
        if (salida.estadoSalida != "FINALIZADA") throw ReglaNegocioException("Solo se pueden evaluar salidas finalizadas")
        if (evaluacionRepository.findBySalidaIdSalida(id) != null) throw ReglaNegocioException("La salida ya fue evaluada")
        evaluacionRepository.save(EvaluacionSalida(salida = salida, calificacionDocente = req.calificacion, comentarios = req.comentario))
        asignacionRepository.findBySalidaIdSalida(id)?.guia?.let { g ->
            g.calificacion = nuevoPromedio(g.calificacion, g.cantidadEvaluaciones, req.calificacion)
            g.cantidadEvaluaciones += 1
            guiaRepository.save(g)
        }
        return aRespuesta(salida)
    }

    fun nuevoPromedio(promedioActual: Double, cantidad: Int, nuevaNota: Int): Double {
        val total = promedioActual * cantidad + nuevaNota
        return Math.round(total / (cantidad + 1) * 10) / 10.0
    }

    private fun obtener(id: Long) = salidaRepository.findById(id).orElseThrow { NoEncontradoException("Salida $id no existe") }

    private fun aRespuesta(s: SalidaPedagogica): SalidaResponse {
        val asignacion = asignacionRepository.findBySalidaIdSalida(s.idSalida!!)
        val evaluacion = evaluacionRepository.findBySalidaIdSalida(s.idSalida!!)
        val paradas = s.ruta?.paradas ?: emptyList()
        return SalidaResponse(
            idSalida = s.idSalida!!,
            idProfesor = s.idProfesor,
            nombreProfesor = s.nombreProfesor,
            colegio = s.colegio,
            curso = s.curso,
            cantidadAlumnos = s.cantidadAlumnos,
            idRuta = s.ruta?.idRuta ?: 0,
            nombreRuta = s.ruta?.nombreRuta ?: "",
            tiempoRutaMin = s.ruta?.tiempoEstimadoMin ?: 0,
            idNodosRuta = paradas.map { it.idNodo },
            nodosRuta = paradas.map { it.nombreNodo },
            fechaProgramada = s.fechaProgramada.toString(),
            horaInicio = s.horaInicio.toString(),
            objetivos = s.objetivos.map { "${it.codigoCurricular} - ${it.descripcion}" }.sorted(),
            notasGuia = s.notasGuia,
            estadoSalida = s.estadoSalida,
            idGuia = asignacion?.guia?.idGuia,
            nombreGuia = asignacion?.guia?.nombre,
            estadoAsignacion = asignacion?.estadoConfirmacion ?: "SIN_GUIA",
            comentarioGuia = asignacion?.comentarioGuia ?: "",
            comentarioFinal = s.comentarioFinal,
            calificacion = evaluacion?.calificacionDocente
        )
    }
}
