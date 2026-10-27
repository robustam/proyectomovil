package cl.arzobispado.redpatrimonial.rutas.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class ParadaDto(
    val idParada: Long = 0,
    @field:Positive val idNodo: Long = 0,
    @field:NotBlank val nombreNodo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val ordenSecuencia: Int = 0,
    @field:Min(5) @field:Max(240) val tiempoSugeridoMin: Int = 30,
    val notasRecorrido: String = ""
)

data class RutaResponse(
    val idRuta: Long,
    val idCreador: Long?,
    val nombreRuta: String,
    val descripcion: String,
    val distanciaKm: Double,
    val tiempoEstimadoMin: Int,
    val nivelDificultad: String,
    val enfoque: String,
    val personalizada: Boolean,
    val paradas: List<ParadaDto>
)

/** CU06/CU09 - Crear ruta personalizada o ruta de aprendizaje. */
data class RutaRequest(
    val idCreador: Long? = null,
    @field:NotBlank @field:Size(max = 120) val nombreRuta: String = "",
    val descripcion: String = "",
    val enfoque: String = "Personalizada",
    @field:Size(min = 1, message = "debe tener al menos una parada")
    @field:Valid val paradas: List<ParadaDto> = emptyList()
)

data class GuiaRequest(
    @field:Positive val idUsuario: Long = 0,
    @field:NotBlank val nombre: String = "",
    val especialidad: String? = null,
    val idiomas: String? = null
)

data class GuiaResponse(
    val idGuia: Long,
    val idUsuario: Long,
    val nombre: String,
    val especialidad: String,
    val idiomas: String,
    val certificacion: String,
    val estadoActivo: Boolean,
    val calificacion: Double,
    val guiadosRealizados: Int,
    /** Solo en la búsqueda por fecha: si tiene un bloque disponible ese día/hora. */
    val disponibleEnFecha: Boolean = false
)

data class DisponibilidadRequest(
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "formato AAAA-MM-DD") val fecha: String = "",
    @field:Pattern(regexp = "\\d{2}:\\d{2}", message = "formato HH:MM") val horaInicio: String = "",
    @field:Pattern(regexp = "\\d{2}:\\d{2}", message = "formato HH:MM") val horaFin: String = ""
)

data class DisponibilidadResponse(
    val idDisponibilidad: Long,
    val idGuia: Long,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estadoBloque: String
)

data class ObjetivoDto(val idObjetivo: Long, val codigoCurricular: String, val descripcion: String, val asignatura: String)

/** CU07/CU08 - Planificar salida pedagógica y solicitar guía. */
data class SalidaRequest(
    @field:Positive val idProfesor: Long = 0,
    val nombreProfesor: String = "",
    @field:NotBlank val colegio: String = "",
    @field:NotBlank val curso: String = "",
    @field:Min(1) @field:Max(200) val cantidadAlumnos: Int = 1,
    @field:Positive val idRuta: Long = 0,
    @field:Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "formato AAAA-MM-DD") val fechaProgramada: String = "",
    @field:Pattern(regexp = "\\d{2}:\\d{2}", message = "formato HH:MM") val horaInicio: String = "",
    val idObjetivos: List<Long> = emptyList(),
    val idGuia: Long? = null,
    val notasGuia: String = ""
)

/** Salida pedagógica con su ruta, objetivos y asignación de guía en un solo objeto. */
data class SalidaResponse(
    val idSalida: Long,
    val idProfesor: Long,
    val nombreProfesor: String,
    val colegio: String,
    val curso: String,
    val cantidadAlumnos: Int,
    val idRuta: Long,
    val nombreRuta: String,
    val tiempoRutaMin: Int,
    val idNodosRuta: List<Long>,
    val nodosRuta: List<String>,
    val fechaProgramada: String,
    val horaInicio: String,
    val objetivos: List<String>,
    val notasGuia: String,
    val estadoSalida: String,
    val idGuia: Long?,
    val nombreGuia: String?,
    /** SIN_GUIA, PENDIENTE, ACEPTADA, REAGENDAR o FINALIZADA */
    val estadoAsignacion: String,
    val comentarioGuia: String,
    val comentarioFinal: String,
    val calificacion: Int?
)

data class CambioEstadoRequest(
    @field:NotBlank val estado: String = "",
    val comentario: String = ""
)

data class EvaluacionRequest(
    @field:Min(1) @field:Max(5) val calificacion: Int = 5,
    val comentario: String = ""
)
