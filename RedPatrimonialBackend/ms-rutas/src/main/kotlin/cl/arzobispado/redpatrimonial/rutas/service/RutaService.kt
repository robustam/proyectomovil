package cl.arzobispado.redpatrimonial.rutas.service

import cl.arzobispado.redpatrimonial.rutas.config.NoEncontradoException
import cl.arzobispado.redpatrimonial.rutas.dto.ParadaDto
import cl.arzobispado.redpatrimonial.rutas.dto.RutaRequest
import cl.arzobispado.redpatrimonial.rutas.dto.RutaResponse
import cl.arzobispado.redpatrimonial.rutas.model.ParadaRuta
import cl.arzobispado.redpatrimonial.rutas.model.RutaPatrimonial
import cl.arzobispado.redpatrimonial.rutas.repository.RutaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RutaService(private val rutaRepository: RutaRepository) {

    /** Rutas oficiales + las rutas personalizadas del usuario indicado. */
    @Transactional(readOnly = true)
    fun listar(idCreador: Long?): List<RutaResponse> =
        rutaRepository.findAll()
            .filter { !it.personalizada || (idCreador != null && it.idCreador == idCreador) }
            .sortedWith(compareBy<RutaPatrimonial> { it.personalizada }.thenBy { it.nombreRuta })
            .map { aRespuesta(it) }

    @Transactional(readOnly = true)
    fun buscar(id: Long): RutaResponse = aRespuesta(obtener(id))

    /** CU06 / CU09 - Trazar ruta personalizada: el servidor calcula distancia y tiempo. */
    @Transactional
    fun crear(req: RutaRequest): RutaResponse {
        val ruta = RutaPatrimonial(
            idCreador = req.idCreador,
            nombreRuta = req.nombreRuta.trim(),
            descripcion = req.descripcion,
            enfoque = req.enfoque.ifBlank { "Personalizada" },
            personalizada = true
        )
        req.paradas.forEachIndexed { i, p ->
            ruta.paradas.add(
                ParadaRuta(
                    ruta = ruta, idNodo = p.idNodo, nombreNodo = p.nombreNodo,
                    latitud = p.latitud, longitud = p.longitud, ordenSecuencia = i + 1,
                    tiempoSugeridoMin = p.tiempoSugeridoMin, notasRecorrido = p.notasRecorrido
                )
            )
        }
        recalcular(ruta)
        return aRespuesta(rutaRepository.save(ruta))
    }

    /** Vista previa del cálculo sin guardar la ruta. */
    fun calcular(req: RutaRequest): RutaResponse {
        val r = CalculadoraRuta.calcular(req.paradas.map { PuntoRuta(it.latitud, it.longitud, it.tiempoSugeridoMin) })
        return RutaResponse(0, req.idCreador, req.nombreRuta, req.descripcion, r.distanciaKm, r.minutosTotal,
            r.dificultad, req.enfoque, true, req.paradas.mapIndexed { i, p -> p.copy(ordenSecuencia = i + 1) })
    }

    @Transactional
    fun eliminar(id: Long) {
        if (!rutaRepository.existsById(id)) throw NoEncontradoException("Ruta $id no existe")
        rutaRepository.deleteById(id)
    }

    fun recalcular(ruta: RutaPatrimonial) {
        val r = CalculadoraRuta.calcular(ruta.paradas.map { PuntoRuta(it.latitud, it.longitud, it.tiempoSugeridoMin) })
        ruta.distanciaKm = r.distanciaKm
        ruta.tiempoEstimadoMin = r.minutosTotal
        ruta.nivelDificultad = r.dificultad
    }

    private fun obtener(id: Long) = rutaRepository.findById(id).orElseThrow { NoEncontradoException("Ruta $id no existe") }

    fun aRespuesta(r: RutaPatrimonial) = RutaResponse(
        idRuta = r.idRuta!!,
        idCreador = r.idCreador,
        nombreRuta = r.nombreRuta,
        descripcion = r.descripcion,
        distanciaKm = r.distanciaKm,
        tiempoEstimadoMin = r.tiempoEstimadoMin,
        nivelDificultad = r.nivelDificultad,
        enfoque = r.enfoque,
        personalizada = r.personalizada,
        paradas = r.paradas.map {
            ParadaDto(it.idParada ?: 0, it.idNodo, it.nombreNodo, it.latitud, it.longitud,
                it.ordenSecuencia, it.tiempoSugeridoMin, it.notasRecorrido)
        }
    )
}
