package cl.arzobispado.redpatrimonial.nodos.dto

import jakarta.validation.constraints.*

data class HorarioRegularDto(
    val diaSemana: Int,
    val horaApertura: String,
    val horaCierre: String
)

data class HorarioExcepcionalDto(
    val idHorarioExc: Long,
    val idNodo: Long,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estadoApertura: String,
    val motivo: String
)

data class RecursoDto(
    val idRecurso: Long,
    val tipoRecurso: String,
    val titulo: String,
    val urlRecurso: String,
    val duracionSegundos: Int?,
    val esPrincipal: Boolean
)

/** Ficha patrimonial completa que consume la app. */
data class NodoResponse(
    val idNodo: Long,
    val nombreOficial: String,
    val direccion: String,
    val comuna: String,
    val tipoNodo: String,
    val decanato: String,
    val latitud: Double,
    val longitud: Double,
    val resenaHistorica: String,
    val estiloArquitectonico: String,
    val anioConstruccion: Int?,
    /** Estado configurado por el encargado. */
    val estadoOperativo: String,
    /** Estado real en este momento (considera los horarios excepcionales de hoy). */
    val estadoActual: String,
    val horariosRegulares: List<HorarioRegularDto>,
    val horariosExcepcionales: List<HorarioExcepcionalDto>,
    val recursos: List<RecursoDto>
)

data class NodoRequest(
    @field:NotBlank @field:Size(max = 150) val nombreOficial: String = "",
    @field:NotBlank @field:Size(max = 200) val direccion: String = "",
    @field:NotBlank val comuna: String = "",
    @field:NotBlank val tipoNodo: String = "TEMPLO",
    @field:DecimalMin("-90.0") @field:DecimalMax("90.0") val latitud: Double = 0.0,
    @field:DecimalMin("-180.0") @field:DecimalMax("180.0") val longitud: Double = 0.0,
    val resenaHistorica: String = "",
    val estiloArquitectonico: String = "",
    val anioConstruccion: Int? = null
)

data class EstadoRequest(
    /** ABIERTO, CERRADO o EN_CELEBRACION */
    @field:NotBlank val estado: String = ""
)

data class HorarioExcepcionalRequest(
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "formato AAAA-MM-DD")
    val fecha: String = "",
    @field:Pattern(regexp = "\\d{2}:\\d{2}", message = "formato HH:MM")
    val horaInicio: String = "",
    @field:Pattern(regexp = "\\d{2}:\\d{2}", message = "formato HH:MM")
    val horaFin: String = "",
    @field:NotBlank val estadoApertura: String = "ABIERTO",
    @field:Size(max = 200) val motivo: String = ""
)

data class RecursoRequest(
    @field:NotBlank val tipoRecurso: String = "IMAGEN",
    @field:NotBlank @field:Size(max = 100) val titulo: String = "",
    @field:Size(max = 255) val urlRecurso: String = "",
    val duracionSegundos: Int? = null,
    val esPrincipal: Boolean = false
)

data class CatalogoDto(val id: Long, val nombre: String)

/** CU21: indicadores de la red para el administrador. */
data class IndicadoresDto(
    val totalNodos: Int,
    val porEstado: Map<String, Int>,
    val porTipo: Map<String, Int>,
    val porComuna: Map<String, Int>
)
