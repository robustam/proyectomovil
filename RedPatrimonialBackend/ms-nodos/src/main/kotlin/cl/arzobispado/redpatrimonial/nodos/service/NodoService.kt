package cl.arzobispado.redpatrimonial.nodos.service

import cl.arzobispado.redpatrimonial.nodos.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.nodos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.nodos.dto.*
import cl.arzobispado.redpatrimonial.nodos.model.*
import cl.arzobispado.redpatrimonial.nodos.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

@Service
class NodoService(
    private val nodoRepository: NodoRepository,
    private val comunaRepository: ComunaRepository,
    private val tipoNodoRepository: TipoNodoRepository,
    private val decanatoRepository: DecanatoRepository,
    private val horarioExcRepository: HorarioExcepcionalRepository,
    private val recursoRepository: RecursoRepository
) {

    // ---------- Consultas (CU01, CU02, CU03, CU04) ----------

    /** Lista de nodos con filtros opcionales: texto, tipo, comuna y "solo abiertos ahora". */
    @Transactional(readOnly = true)
    fun listar(q: String?, tipo: String?, comuna: String?, soloAbiertos: Boolean): List<NodoResponse> {
        val ahora = LocalDateTime.now()
        return nodoRepository.findAll()
            .asSequence()
            .filter { q.isNullOrBlank() || it.nombreOficial.contains(q, true) || it.direccion.contains(q, true) || (it.comuna?.nombreComuna ?: "").contains(q, true) }
            .filter { tipo.isNullOrBlank() || it.tipoNodo?.nombreTipo.equals(tipo, true) }
            .filter { comuna.isNullOrBlank() || it.comuna?.nombreComuna.equals(comuna, true) }
            .map { aRespuesta(it, ahora) }
            .filter { !soloAbiertos || it.estadoActual == EstadoApertura.ABIERTO.name }
            .sortedBy { it.nombreOficial }
            .toList()
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long): NodoResponse = aRespuesta(obtener(id), LocalDateTime.now())

    // ---------- Gestión del encargado (CU11, CU13) ----------

    @Transactional
    fun crear(req: NodoRequest): NodoResponse {
        val nodo = NodoPatrimonial()
        aplicar(nodo, req)
        return aRespuesta(nodoRepository.save(nodo), LocalDateTime.now())
    }

    /** CU13 - Modificar ficha. */
    @Transactional
    fun actualizar(id: Long, req: NodoRequest): NodoResponse {
        val nodo = obtener(id)
        aplicar(nodo, req)
        return aRespuesta(nodoRepository.save(nodo), LocalDateTime.now())
    }

    @Transactional
    fun eliminar(id: Long) {
        if (!nodoRepository.existsById(id)) throw NoEncontradoException("Nodo $id no existe")
        nodoRepository.deleteById(id)
    }

    /** CU04/CU11 - El encargado cambia el estado de apertura en tiempo real. */
    @Transactional
    fun cambiarEstado(id: Long, estado: String): NodoResponse {
        val nodo = obtener(id)
        nodo.estadoOperativo = parseEstado(estado)
        return aRespuesta(nodoRepository.save(nodo), LocalDateTime.now())
    }

    @Transactional(readOnly = true)
    fun horariosExcepcionales(idNodo: Long): List<HorarioExcepcionalDto> =
        obtener(idNodo).horariosExcepcionales.map { aDto(it) }

    /** CU11 - Registrar un bloque horario excepcional (apertura especial, cierre, celebración). */
    @Transactional
    fun agregarHorarioExcepcional(idNodo: Long, req: HorarioExcepcionalRequest): HorarioExcepcionalDto {
        val nodo = obtener(idNodo)
        val fecha = parseFecha(req.fecha)
        val inicio = parseHora(req.horaInicio)
        val fin = parseHora(req.horaFin)
        if (!fin.isAfter(inicio)) throw ReglaNegocioException("La hora de término debe ser posterior a la de inicio")
        if (fecha.isBefore(LocalDate.now())) throw ReglaNegocioException("No se pueden registrar horarios en fechas pasadas")

        val horario = HorarioExcepcional(
            nodo = nodo, fecha = fecha, horaInicio = inicio, horaFin = fin,
            motivo = req.motivo, estadoApertura = parseEstado(req.estadoApertura)
        )
        val guardado = horarioExcRepository.save(horario)
        nodo.horariosExcepcionales.add(guardado)
        return aDto(guardado)
    }

    @Transactional
    fun eliminarHorarioExcepcional(idHorario: Long) {
        val horario = horarioExcRepository.findById(idHorario)
            .orElseThrow { NoEncontradoException("Horario $idHorario no existe") }
        horario.nodo?.horariosExcepcionales?.remove(horario)
        horarioExcRepository.delete(horario)
    }

    /** CU13 - Agregar un recurso a la galería multimedia (simulada con URL). */
    @Transactional
    fun agregarRecurso(idNodo: Long, req: RecursoRequest): RecursoDto {
        val nodo = obtener(idNodo)
        val tipo = req.tipoRecurso.uppercase()
        if (tipo !in setOf("IMAGEN", "AUDIO", "VIDEO")) throw ReglaNegocioException("Tipo de recurso inválido: $tipo")
        if (req.esPrincipal) nodo.recursos.forEach { it.esPrincipal = false }
        val recurso = RecursoMultimedia(
            nodo = nodo, tipoRecurso = tipo, titulo = req.titulo,
            urlRecurso = req.urlRecurso.ifBlank { "https://ejemplo.cl/recursos/simulado.jpg" },
            duracionSegundos = req.duracionSegundos, esPrincipal = req.esPrincipal
        )
        val guardado = recursoRepository.save(recurso)
        nodo.recursos.add(guardado)
        return aDto(guardado)
    }

    @Transactional
    fun eliminarRecurso(idRecurso: Long) {
        val recurso = recursoRepository.findById(idRecurso)
            .orElseThrow { NoEncontradoException("Recurso $idRecurso no existe") }
        recurso.nodo?.recursos?.remove(recurso)
        recursoRepository.delete(recurso)
    }

    // ---------- Catálogos e indicadores ----------

    @Transactional(readOnly = true)
    fun comunas() = comunaRepository.findAll().map { CatalogoDto(it.idComuna!!, it.nombreComuna) }.sortedBy { it.nombre }

    @Transactional(readOnly = true)
    fun tipos() = tipoNodoRepository.findAll().map { CatalogoDto(it.idTipoNodo!!, it.nombreTipo) }

    @Transactional(readOnly = true)
    fun decanatos() = decanatoRepository.findAll().map { CatalogoDto(it.idDecanato!!, it.nombreDecanato) }

    /** CU21 - Monitorear indicadores de la red. */
    @Transactional(readOnly = true)
    fun indicadores(): IndicadoresDto {
        val nodos = listar(null, null, null, false)
        return IndicadoresDto(
            totalNodos = nodos.size,
            porEstado = nodos.groupingBy { it.estadoActual }.eachCount(),
            porTipo = nodos.groupingBy { it.tipoNodo }.eachCount(),
            porComuna = nodos.groupingBy { it.comuna }.eachCount()
        )
    }

    // ---------- Lógica de estado en tiempo real ----------

    /**
     * Si hoy existe un horario excepcional que cubre la hora actual, ese estado manda.
     * Si no, se usa el estado que configuró el encargado.
     */
    fun calcularEstadoActual(nodo: NodoPatrimonial, ahora: LocalDateTime): EstadoApertura {
        val hoy = ahora.toLocalDate()
        val hora = ahora.toLocalTime()
        val excepcional = nodo.horariosExcepcionales.firstOrNull {
            it.fecha == hoy && !hora.isBefore(it.horaInicio) && hora.isBefore(it.horaFin)
        }
        return excepcional?.estadoApertura ?: nodo.estadoOperativo
    }

    // ---------- Conversión y utilidades ----------

    private fun obtener(id: Long): NodoPatrimonial =
        nodoRepository.findById(id).orElseThrow { NoEncontradoException("Nodo $id no existe") }

    private fun aplicar(nodo: NodoPatrimonial, req: NodoRequest) {
        nodo.nombreOficial = req.nombreOficial.trim()
        nodo.direccion = req.direccion.trim()
        nodo.comuna = comunaRepository.findByNombreComunaIgnoreCase(req.comuna.trim())
            ?: throw ReglaNegocioException("La comuna ${req.comuna} no está registrada")
        nodo.tipoNodo = tipoNodoRepository.findByNombreTipoIgnoreCase(req.tipoNodo.trim())
            ?: throw ReglaNegocioException("El tipo de nodo ${req.tipoNodo} no existe")
        nodo.latitud = BigDecimal.valueOf(req.latitud)
        nodo.longitud = BigDecimal.valueOf(req.longitud)
        nodo.resenaHistorica = req.resenaHistorica
        nodo.estiloArquitectonico = req.estiloArquitectonico
        nodo.fechaConstruccion = req.anioConstruccion
    }

    private fun parseEstado(valor: String): EstadoApertura =
        try { EstadoApertura.valueOf(valor.trim().uppercase().replace(' ', '_')) }
        catch (e: IllegalArgumentException) { throw ReglaNegocioException("Estado inválido: $valor (use ABIERTO, CERRADO o EN_CELEBRACION)") }

    private fun parseFecha(valor: String): LocalDate =
        try { LocalDate.parse(valor) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Fecha inválida: $valor") }

    private fun parseHora(valor: String): LocalTime =
        try { LocalTime.parse(valor) } catch (e: DateTimeParseException) { throw ReglaNegocioException("Hora inválida: $valor") }

    private fun aRespuesta(n: NodoPatrimonial, ahora: LocalDateTime) = NodoResponse(
        idNodo = n.idNodo!!,
        nombreOficial = n.nombreOficial,
        direccion = n.direccion,
        comuna = n.comuna?.nombreComuna ?: "",
        tipoNodo = n.tipoNodo?.nombreTipo ?: "",
        decanato = n.decanato?.nombreDecanato ?: "",
        latitud = n.latitud.toDouble(),
        longitud = n.longitud.toDouble(),
        resenaHistorica = n.resenaHistorica,
        estiloArquitectonico = n.estiloArquitectonico,
        anioConstruccion = n.fechaConstruccion,
        estadoOperativo = n.estadoOperativo.name,
        estadoActual = calcularEstadoActual(n, ahora).name,
        horariosRegulares = n.horariosRegulares.map {
            HorarioRegularDto(it.diaSemana, it.horaApertura.toString(), it.horaCierre.toString())
        },
        horariosExcepcionales = n.horariosExcepcionales
            .filter { !it.fecha.isBefore(ahora.toLocalDate()) }
            .map { aDto(it) },
        recursos = n.recursos.map { aDto(it) }
    )

    private fun aDto(h: HorarioExcepcional) = HorarioExcepcionalDto(
        idHorarioExc = h.idHorarioExc ?: 0,
        idNodo = h.nodo?.idNodo ?: 0,
        fecha = h.fecha.toString(),
        horaInicio = h.horaInicio.toString(),
        horaFin = h.horaFin.toString(),
        estadoApertura = h.estadoApertura.name,
        motivo = h.motivo
    )

    private fun aDto(r: RecursoMultimedia) = RecursoDto(
        idRecurso = r.idRecurso ?: 0,
        tipoRecurso = r.tipoRecurso,
        titulo = r.titulo,
        urlRecurso = r.urlRecurso,
        duracionSegundos = r.duracionSegundos,
        esPrincipal = r.esPrincipal
    )
}
