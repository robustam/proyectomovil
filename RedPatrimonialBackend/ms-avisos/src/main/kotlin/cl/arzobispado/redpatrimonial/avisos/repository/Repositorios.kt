package cl.arzobispado.redpatrimonial.avisos.repository

import cl.arzobispado.redpatrimonial.avisos.model.AvisoComunitario
import cl.arzobispado.redpatrimonial.avisos.model.CategoriaAviso
import cl.arzobispado.redpatrimonial.avisos.model.ReporteAviso
import org.springframework.data.jpa.repository.JpaRepository

interface CategoriaAvisoRepository : JpaRepository<CategoriaAviso, Long> {
    fun findByNombreCatIgnoreCase(nombre: String): CategoriaAviso?
}
interface AvisoRepository : JpaRepository<AvisoComunitario, Long> {
    fun findAllByOrderByFechaPublicacionDesc(): List<AvisoComunitario>
}
interface ReporteAvisoRepository : JpaRepository<ReporteAviso, Long> {
    fun findByAvisoIdAviso(idAviso: Long): List<ReporteAviso>
    fun existsByAvisoIdAvisoAndIdUsuarioReporta(idAviso: Long, idUsuario: Long): Boolean
}
