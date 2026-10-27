package cl.arzobispado.redpatrimonial.rutas.repository

import cl.arzobispado.redpatrimonial.rutas.model.*
import org.springframework.data.jpa.repository.JpaRepository

interface RutaRepository : JpaRepository<RutaPatrimonial, Long>
interface GuiaRepository : JpaRepository<GuiaVoluntario, Long> {
    fun findByIdUsuario(idUsuario: Long): GuiaVoluntario?
}
interface DisponibilidadRepository : JpaRepository<DisponibilidadGuia, Long> {
    fun findByGuiaIdGuiaOrderByFechaAscHoraInicioAsc(idGuia: Long): List<DisponibilidadGuia>
    fun findByFecha(fecha: java.time.LocalDate): List<DisponibilidadGuia>
}
interface ObjetivoRepository : JpaRepository<ObjetivoPedagogico, Long>
interface SalidaRepository : JpaRepository<SalidaPedagogica, Long>
interface AsignacionRepository : JpaRepository<AsignacionGuia, Long> {
    fun findBySalidaIdSalida(idSalida: Long): AsignacionGuia?
    fun findByGuiaIdGuia(idGuia: Long): List<AsignacionGuia>
}
interface EvaluacionRepository : JpaRepository<EvaluacionSalida, Long> {
    fun findBySalidaIdSalida(idSalida: Long): EvaluacionSalida?
}
