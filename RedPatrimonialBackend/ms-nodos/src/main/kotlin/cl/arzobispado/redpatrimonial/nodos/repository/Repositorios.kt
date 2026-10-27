package cl.arzobispado.redpatrimonial.nodos.repository

import cl.arzobispado.redpatrimonial.nodos.model.*
import org.springframework.data.jpa.repository.JpaRepository

interface RegionRepository : JpaRepository<Region, Long>
interface ComunaRepository : JpaRepository<Comuna, Long> {
    fun findByNombreComunaIgnoreCase(nombre: String): Comuna?
}
interface ZonaPastoralRepository : JpaRepository<ZonaPastoral, Long>
interface DecanatoRepository : JpaRepository<Decanato, Long>
interface TipoNodoRepository : JpaRepository<TipoNodo, Long> {
    fun findByNombreTipoIgnoreCase(nombre: String): TipoNodo?
}
interface NodoRepository : JpaRepository<NodoPatrimonial, Long>
interface HorarioExcepcionalRepository : JpaRepository<HorarioExcepcional, Long>
interface RecursoRepository : JpaRepository<RecursoMultimedia, Long>
