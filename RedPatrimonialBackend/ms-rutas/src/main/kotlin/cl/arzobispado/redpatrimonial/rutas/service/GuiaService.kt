package cl.arzobispado.redpatrimonial.rutas.service

import cl.arzobispado.redpatrimonial.rutas.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.rutas.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.rutas.dto.*
import cl.arzobispado.redpatrimonial.rutas.model.DisponibilidadGuia
import cl.arzobispado.redpatrimonial.rutas.model.GuiaVoluntario
import cl.arzobispado.redpatrimonial.rutas.repository.DisponibilidadRepository
import cl.arzobispado.redpatrimonial.rutas.repository.GuiaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Service
class GuiaService(
    private val guiaRepository: GuiaRepository,
    private val disponibilidadRepository: DisponibilidadRepository
) {

    @Transactional
    fun crear(req: GuiaRequest): GuiaResponse {
        guiaRepository.findByIdUsuario(req.idUsuario)?.let { return aRespuesta(it) }
        val guia = GuiaVoluntario(idUsuario = req.idUsuario, nombre = req.nombre)
        req.especialidad?.let { guia.especialidad = it }
        req.idiomas?.let { guia.idiomas = it }
        return aRespuesta(guiaRepository.save(guia))
    }

    @Transactional(readOnly = true)
    fun listar(soloActivos: Boolean): List<GuiaResponse> =
        guiaRepository.findAll().filter { !soloActivos || it.estadoActivo }
            .sortedByDescending { it.calificacion }.map { aRespuesta(it) }

    /** Perfil del guía según su usuario; si no existe aún, se crea (primer ingreso). */
    @Transactional
    fun porUsuario(idUsuario: Long, nombre: String?): GuiaResponse {
        val guia = guiaRepository.findByIdUsuario(idUsuario)
            ?: guiaRepository.save(GuiaVoluntario(idUsuario = idUsuario, nombre = nombre ?: "Guía $idUsuario"))
        return aRespuesta(guia)
    }

    @Transactional
    fun cambiarActivo(idGuia: Long, activo: Boolean): GuiaResponse {
        val guia = obtener(idGuia)
        guia.estadoActivo = activo
        return aRespuesta(guiaRepository.save(guia))
    }

    @Transactional(readOnly = true)
    fun disponibilidad(idGuia: Long): List<DisponibilidadResponse> =
        disponibilidadRepository.findByGuiaIdGuiaOrderByFechaAscHoraInicioAsc(idGuia)
            .filter { !it.fecha.isBefore(LocalDate.now()) }
            .map { aDto(it) }

    /** CU16 - Registrar disponibilidad horaria del guía. */
    @Transactional
    fun agregarDisponibilidad(idGuia: Long, req: DisponibilidadRequest): DisponibilidadResponse {
        val guia = obtener(idGuia)
        val fecha = try { LocalDate.parse(req.fecha) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Fecha inválida") }
        val inicio = try { LocalTime.parse(req.horaInicio) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Hora inválida") }
        val fin = try { LocalTime.parse(req.horaFin) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Hora inválida") }
        if (fecha.isBefore(LocalDate.now())) throw ReglaNegocioException("La fecha no puede ser pasada")
        if (!fin.isAfter(inicio)) throw ReglaNegocioException("La hora de término debe ser posterior a la de inicio")
        val choca = disponibilidadRepository.findByGuiaIdGuiaOrderByFechaAscHoraInicioAsc(idGuia)
            .any { it.fecha == fecha && inicio.isBefore(it.horaFin) && it.horaInicio.isBefore(fin) }
        if (choca) throw ReglaNegocioException("Ese bloque se cruza con otro ya registrado")
        return aDto(disponibilidadRepository.save(DisponibilidadGuia(guia = guia, fecha = fecha, horaInicio = inicio, horaFin = fin)))
    }

    @Transactional
    fun eliminarDisponibilidad(id: Long) {
        if (!disponibilidadRepository.existsById(id)) throw NoEncontradoException("Bloque $id no existe")
        disponibilidadRepository.deleteById(id)
    }

    /** Guías activos marcando cuáles tienen un bloque libre en la fecha y hora pedidas. */
    @Transactional(readOnly = true)
    fun disponiblesPara(fecha: String, hora: String): List<GuiaResponse> {
        val dia = try { LocalDate.parse(fecha) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Fecha inválida") }
        val h = try { LocalTime.parse(hora) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Hora inválida") }
        val libres = disponibilidadRepository.findByFecha(dia)
            .filter { it.estadoBloque == "DISPONIBLE" && !h.isBefore(it.horaInicio) && h.isBefore(it.horaFin) }
            .mapNotNull { it.guia?.idGuia }.toSet()
        return guiaRepository.findAll().filter { it.estadoActivo }
            .map { aRespuesta(it, it.idGuia in libres) }
            .sortedWith(compareByDescending<GuiaResponse> { it.disponibleEnFecha }.thenByDescending { it.calificacion })
    }

    fun obtener(idGuia: Long): GuiaVoluntario =
        guiaRepository.findById(idGuia).orElseThrow { NoEncontradoException("Guía $idGuia no existe") }

    fun aRespuesta(g: GuiaVoluntario, disponible: Boolean = false) = GuiaResponse(
        idGuia = g.idGuia!!, idUsuario = g.idUsuario, nombre = g.nombre, especialidad = g.especialidad,
        idiomas = g.idiomas, certificacion = g.certificacion, estadoActivo = g.estadoActivo,
        calificacion = g.calificacion, guiadosRealizados = g.guiadosRealizados, disponibleEnFecha = disponible
    )

    private fun aDto(d: DisponibilidadGuia) = DisponibilidadResponse(
        idDisponibilidad = d.idDisponibilidad!!, idGuia = d.guia?.idGuia ?: 0, fecha = d.fecha.toString(),
        horaInicio = d.horaInicio.toString(), horaFin = d.horaFin.toString(), estadoBloque = d.estadoBloque
    )
}
