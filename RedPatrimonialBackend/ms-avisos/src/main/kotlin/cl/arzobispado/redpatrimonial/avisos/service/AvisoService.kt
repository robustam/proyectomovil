package cl.arzobispado.redpatrimonial.avisos.service

import cl.arzobispado.redpatrimonial.avisos.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.avisos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.avisos.dto.*
import cl.arzobispado.redpatrimonial.avisos.model.AvisoComunitario
import cl.arzobispado.redpatrimonial.avisos.model.ReporteAviso
import cl.arzobispado.redpatrimonial.avisos.repository.AvisoRepository
import cl.arzobispado.redpatrimonial.avisos.repository.CategoriaAvisoRepository
import cl.arzobispado.redpatrimonial.avisos.repository.ReporteAvisoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class AvisoService(
    private val avisoRepository: AvisoRepository,
    private val categoriaRepository: CategoriaAvisoRepository,
    private val reporteRepository: ReporteAvisoRepository
) {
    companion object {
        /** Con esta cantidad de reportes el aviso se oculta hasta que se revise. */
        const val LIMITE_REPORTES = 3
    }

    @Transactional(readOnly = true)
    fun listar(soloVigentes: Boolean, categoria: String?, idNodo: Long?): List<AvisoResponse> {
        val ahora = LocalDateTime.now()
        return avisoRepository.findAllByOrderByFechaPublicacionDesc()
            .filter { categoria.isNullOrBlank() || it.categoria?.nombreCat.equals(categoria, true) }
            .filter { idNodo == null || it.idNodo == idNodo }
            .filter { !soloVigentes || esVigente(it, ahora) }
            .map { aRespuesta(it, ahora) }
    }

    @Transactional(readOnly = true)
    fun buscar(id: Long) = aRespuesta(obtener(id), LocalDateTime.now())

    /** CU14 - El encargado publica un aviso que se ve de inmediato en toda la red. */
    @Transactional
    fun publicar(req: AvisoRequest): AvisoResponse {
        val categoria = categoriaRepository.findByNombreCatIgnoreCase(req.categoria)
            ?: throw ReglaNegocioException("Categoría ${req.categoria} no existe")
        val ahora = LocalDateTime.now()
        val aviso = avisoRepository.save(
            AvisoComunitario(
                idNodo = req.idNodo, nombreNodo = req.nombreNodo, idUsuario = req.idUsuario,
                categoria = categoria, titulo = req.titulo.trim(), mensaje = req.mensaje.trim(),
                fechaPublicacion = ahora, fechaExpiracion = ahora.plusHours(req.horasVigencia.toLong()),
                latitud = req.latitud, longitud = req.longitud
            )
        )
        return aRespuesta(aviso, ahora)
    }

    /** El encargado retira un aviso antes de que expire. */
    @Transactional
    fun retirar(id: Long): AvisoResponse {
        val aviso = obtener(id)
        aviso.estado = "RETIRADO"
        return aRespuesta(avisoRepository.save(aviso), LocalDateTime.now())
    }

    /** Un usuario reporta un aviso. Con 3 reportes queda EN_REVISION y se oculta. */
    @Transactional
    fun reportar(id: Long, req: ReporteRequest): ReporteResponse {
        val aviso = obtener(id)
        if (reporteRepository.existsByAvisoIdAvisoAndIdUsuarioReporta(id, req.idUsuario)) {
            throw ReglaNegocioException("Ya reportaste este aviso")
        }
        val reporte = reporteRepository.save(ReporteAviso(aviso = aviso, idUsuarioReporta = req.idUsuario, motivo = req.motivo))
        if (reporteRepository.findByAvisoIdAviso(id).size >= LIMITE_REPORTES) {
            aviso.estado = "EN_REVISION"
            avisoRepository.save(aviso)
        }
        return ReporteResponse(reporte.idReporte!!, id, req.idUsuario, req.motivo, reporte.fechaReporte.toString())
    }

    @Transactional(readOnly = true)
    fun reportes(id: Long): List<ReporteResponse> = reporteRepository.findByAvisoIdAviso(id).map {
        ReporteResponse(it.idReporte!!, id, it.idUsuarioReporta, it.motivo, it.fechaReporte.toString())
    }

    @Transactional(readOnly = true)
    fun categorias() = categoriaRepository.findAll().map { CategoriaAvisoDto(it.idCatAviso!!, it.nombreCat, it.icono) }

    fun esVigente(a: AvisoComunitario, ahora: LocalDateTime) = a.estado == "ACTIVO" && a.fechaExpiracion.isAfter(ahora)

    private fun obtener(id: Long) = avisoRepository.findById(id).orElseThrow { NoEncontradoException("Aviso $id no existe") }

    private fun aRespuesta(a: AvisoComunitario, ahora: LocalDateTime) = AvisoResponse(
        idAviso = a.idAviso!!,
        idNodo = a.idNodo,
        nombreNodo = a.nombreNodo,
        idUsuario = a.idUsuario,
        categoria = a.categoria?.nombreCat ?: "",
        icono = a.categoria?.icono ?: "",
        titulo = a.titulo,
        mensaje = a.mensaje,
        fechaPublicacion = a.fechaPublicacion.toString(),
        fechaExpiracion = a.fechaExpiracion.toString(),
        haceMinutos = Duration.between(a.fechaPublicacion, ahora).toMinutes().coerceAtLeast(0),
        latitud = a.latitud,
        longitud = a.longitud,
        estado = a.estado,
        vigente = esVigente(a, ahora),
        cantidadReportes = reporteRepository.findByAvisoIdAviso(a.idAviso!!).size
    )
}
