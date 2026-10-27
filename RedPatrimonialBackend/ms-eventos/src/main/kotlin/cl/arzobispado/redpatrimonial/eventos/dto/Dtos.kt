package cl.arzobispado.redpatrimonial.eventos.dto

import jakarta.validation.constraints.*

data class EventoResponse(
    val idEvento: Long,
    val idNodo: Long,
    val nombreNodo: String,
    val latitud: Double?,
    val longitud: Double?,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int,
    val requiereInscripcion: Boolean,
    val estado: String
)

/** CU12 - Publicar evento cultural o litúrgico. */
data class EventoRequest(
    @field:Positive val idNodo: Long = 0,
    val nombreNodo: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    @field:NotBlank val categoria: String = "",
    @field:NotBlank @field:Size(max = 150) val titulo: String = "",
    val descripcion: String = "",
    /** Formato AAAA-MM-DDTHH:MM, por ejemplo 2026-10-24T19:00 */
    @field:NotBlank val fechaInicio: String = "",
    @field:NotBlank val fechaFin: String = "",
    @field:Min(0) val cupoMaximo: Int = 0,
    val requiereInscripcion: Boolean = false,
    val idPublicador: Long? = null
)

data class InscripcionRequest(
    @field:Positive val idUsuario: Long = 0,
    @field:Min(1) @field:Max(50) val cantidadPersonas: Int = 1
)

/** Ticket que la app muestra con el código QR. */
data class InscripcionResponse(
    val idInscripcion: Long,
    val idEvento: Long,
    val idUsuario: Long,
    val tituloEvento: String,
    val nombreNodo: String,
    val fechaEvento: String,
    val cantidadPersonas: Int,
    val fechaInscripcion: String,
    val estadoAsistencia: String,
    val codigoQr: String
)

data class CategoriaDto(val id: Long, val nombre: String, val descripcion: String)
