package cl.arzobispado.redpatrimonial.eventos.repository

import cl.arzobispado.redpatrimonial.eventos.model.CategoriaEvento
import cl.arzobispado.redpatrimonial.eventos.model.EventoCultural
import cl.arzobispado.redpatrimonial.eventos.model.InscripcionEvento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CategoriaEventoRepository : JpaRepository<CategoriaEvento, Long> {
    fun findByNombreCategoriaIgnoreCase(nombre: String): CategoriaEvento?
}

interface EventoRepository : JpaRepository<EventoCultural, Long> {
    fun findAllByOrderByFechaInicioAsc(): List<EventoCultural>
}

interface InscripcionRepository : JpaRepository<InscripcionEvento, Long> {
    fun findByIdUsuarioOrderByFechaInscripcionDesc(idUsuario: Long): List<InscripcionEvento>
    fun findByCodigoQr(codigoQr: String): InscripcionEvento?
    fun findByEventoIdEventoAndIdUsuarioAndEstadoAsistencia(idEvento: Long, idUsuario: Long, estado: String): List<InscripcionEvento>

    @Query("select coalesce(sum(i.cantidadPersonas), 0) from InscripcionEvento i where i.evento.idEvento = :idEvento and i.estadoAsistencia <> 'CANCELADA'")
    fun personasInscritas(@Param("idEvento") idEvento: Long): Long
}
