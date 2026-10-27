package cl.arzobispado.redpatrimonial.avisos.dto

import jakarta.validation.constraints.*

data class AvisoResponse(
    val idAviso: Long,
    val idNodo: Long,
    val nombreNodo: String,
    val idUsuario: Long,
    val categoria: String,
    val icono: String,
    val titulo: String,
    val mensaje: String,
    val fechaPublicacion: String,
    val fechaExpiracion: String,
    /** Minutos desde la publicación, para mostrar "Hace 12 min" sin cálculos en la app. */
    val haceMinutos: Long,
    val latitud: Double?,
    val longitud: Double?,
    val estado: String,
    val vigente: Boolean,
    val cantidadReportes: Int
)

/** CU14 - Notificar aviso/alerta en tiempo real. */
data class AvisoRequest(
    @field:Positive val idNodo: Long = 0,
    @field:NotBlank val nombreNodo: String = "",
    @field:Positive val idUsuario: Long = 0,
    @field:NotBlank val categoria: String = "",
    @field:NotBlank @field:Size(max = 100) val titulo: String = "",
    @field:NotBlank @field:Size(max = 1000) val mensaje: String = "",
    @field:Min(1) @field:Max(168) val horasVigencia: Int = 24,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class ReporteRequest(
    @field:Positive val idUsuario: Long = 0,
    @field:NotBlank @field:Size(max = 200) val motivo: String = ""
)

data class ReporteResponse(val idReporte: Long, val idAviso: Long, val idUsuarioReporta: Long, val motivo: String, val fechaReporte: String)

data class CategoriaAvisoDto(val id: Long, val nombre: String, val icono: String)
