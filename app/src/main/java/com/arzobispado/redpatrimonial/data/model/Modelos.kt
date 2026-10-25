package com.arzobispado.redpatrimonial.data.model

/*
 * MODELOS DE DATOS (la "M" de MVVM).
 * Los nombres de los campos son iguales al JSON que envían los microservicios,
 * así Retrofit + Gson los convierten automáticamente.
 */

// ---------------- Usuarios (ms-usuarios :8081) ----------------

/** Roles de la red, según los actores del anexo del Arzobispado. */
object Roles {
    const val TURISTA = "TURISTA"
    const val PROFESOR = "PROFESOR"
    const val GUIA = "GUIA"
    const val ENCARGADO = "ENCARGADO"
    const val ADMIN = "ADMIN"

    fun nombreVisible(rol: String) = when (rol) {
        TURISTA -> "Turista / Visitante"
        PROFESOR -> "Profesor / Encargado escolar"
        GUIA -> "Guía voluntario"
        ENCARGADO -> "Encargado de templo"
        ADMIN -> "Administrador"
        else -> rol
    }
}

data class Usuario(
    val idUsuario: Long,
    val nombreCompleto: String,
    val email: String,
    val rol: String,
    val estado: String = "ACTIVO",
    val fechaRegistro: String = "",
    val idNodoAsignado: Long? = null
)

data class LoginRequest(val email: String, val password: String)

data class RegistroRequest(
    val nombreCompleto: String,
    val email: String,
    val password: String,
    val rol: String
)

data class PermisoRequest(val idUsuario: Long, val idNodo: Long)

// ---------------- Nodos patrimoniales (ms-nodos :8082) ----------------

/** Estados de apertura del anexo: Abierto, Cerrado, En Celebración. */
object EstadosApertura {
    const val ABIERTO = "ABIERTO"
    const val CERRADO = "CERRADO"
    const val EN_CELEBRACION = "EN_CELEBRACION"
    val TODOS = listOf(ABIERTO, CERRADO, EN_CELEBRACION)

    fun nombreVisible(estado: String) = when (estado) {
        ABIERTO -> "Abierto"
        CERRADO -> "Cerrado"
        EN_CELEBRACION -> "En celebración"
        else -> estado
    }
}

data class NodoPatrimonial(
    val idNodo: Long,
    val nombreOficial: String,
    val direccion: String,
    val comuna: String,
    val tipoNodo: String,
    val decanato: String = "",
    val latitud: Double,
    val longitud: Double,
    val resenaHistorica: String = "",
    val estiloArquitectonico: String = "",
    val anioConstruccion: Int? = null,
    val estadoOperativo: String = EstadosApertura.ABIERTO,
    val estadoActual: String = EstadosApertura.ABIERTO,
    val horariosRegulares: List<HorarioRegular> = emptyList(),
    val horariosExcepcionales: List<HorarioExcepcional> = emptyList(),
    val recursos: List<RecursoMultimedia> = emptyList()
)

data class HorarioRegular(val diaSemana: Int, val horaApertura: String, val horaCierre: String)

data class HorarioExcepcional(
    val idHorarioExc: Long,
    val idNodo: Long,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estadoApertura: String,
    val motivo: String
)

data class RecursoMultimedia(
    val idRecurso: Long,
    val tipoRecurso: String,
    val titulo: String,
    val urlRecurso: String,
    val duracionSegundos: Int? = null,
    val esPrincipal: Boolean = false
)

data class NodoRequest(
    val nombreOficial: String,
    val direccion: String,
    val comuna: String,
    val tipoNodo: String,
    val latitud: Double,
    val longitud: Double,
    val resenaHistorica: String,
    val estiloArquitectonico: String,
    val anioConstruccion: Int?
)

data class EstadoRequest(val estado: String)

data class HorarioExcepcionalRequest(
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estadoApertura: String,
    val motivo: String
)

data class RecursoRequest(
    val tipoRecurso: String,
    val titulo: String,
    val urlRecurso: String,
    val duracionSegundos: Int? = null,
    val esPrincipal: Boolean = false
)

// ---------------- Eventos (ms-eventos :8083) ----------------

data class Evento(
    val idEvento: Long,
    val idNodo: Long,
    val nombreNodo: String,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    /** Formato 2026-10-24T19:00 */
    val fechaInicio: String,
    val fechaFin: String,
    val cupoMaximo: Int,
    val cuposDisponibles: Int,
    val requiereInscripcion: Boolean,
    val estado: String
)

data class EventoRequest(
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
    val requiereInscripcion: Boolean,
    val idPublicador: Long?
)

data class InscripcionRequest(val idUsuario: Long, val cantidadPersonas: Int)

/** Ticket con código QR. */
data class Inscripcion(
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

// ---------------- Rutas, salidas y guías (ms-rutas :8084) ----------------

data class Parada(
    val idParada: Long = 0,
    val idNodo: Long,
    val nombreNodo: String,
    val latitud: Double,
    val longitud: Double,
    val ordenSecuencia: Int = 0,
    val tiempoSugeridoMin: Int = 30,
    val notasRecorrido: String = ""
)

data class Ruta(
    val idRuta: Long,
    val idCreador: Long? = null,
    val nombreRuta: String,
    val descripcion: String,
    val distanciaKm: Double,
    val tiempoEstimadoMin: Int,
    val nivelDificultad: String,
    val enfoque: String,
    val personalizada: Boolean,
    val paradas: List<Parada>
)

data class RutaRequest(
    val idCreador: Long?,
    val nombreRuta: String,
    val descripcion: String,
    val enfoque: String,
    val paradas: List<Parada>
)

data class Guia(
    val idGuia: Long,
    val idUsuario: Long,
    val nombre: String,
    val especialidad: String,
    val idiomas: String,
    val certificacion: String,
    val estadoActivo: Boolean,
    val calificacion: Double,
    val guiadosRealizados: Int,
    val disponibleEnFecha: Boolean = false
)

data class Disponibilidad(
    val idDisponibilidad: Long,
    val idGuia: Long,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val estadoBloque: String
)

data class DisponibilidadRequest(val fecha: String, val horaInicio: String, val horaFin: String)

data class ObjetivoPedagogico(
    val idObjetivo: Long,
    val codigoCurricular: String,
    val descripcion: String,
    val asignatura: String
)

/** Estados de una salida y de la asignación del guía. */
object EstadosSalida {
    const val PENDIENTE = "PENDIENTE"
    const val CONFIRMADA = "CONFIRMADA"
    const val RECHAZADA = "RECHAZADA"
    const val FINALIZADA = "FINALIZADA"
    const val SIN_GUIA = "SIN_GUIA"
    const val ACEPTADA = "ACEPTADA"
    const val REAGENDAR = "REAGENDAR"

    fun nombreVisible(estado: String) = when (estado) {
        PENDIENTE -> "Pendiente"
        CONFIRMADA -> "Confirmada"
        RECHAZADA -> "Rechazada"
        FINALIZADA -> "Finalizada"
        SIN_GUIA -> "Sin guía"
        ACEPTADA -> "Aceptada"
        REAGENDAR -> "Pide reagendar"
        else -> estado
    }
}

data class SalidaPedagogica(
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
    val estadoAsignacion: String,
    val comentarioGuia: String = "",
    val comentarioFinal: String = "",
    val calificacion: Int? = null
)

data class SalidaRequest(
    val idProfesor: Long,
    val nombreProfesor: String,
    val colegio: String,
    val curso: String,
    val cantidadAlumnos: Int,
    val idRuta: Long,
    val fechaProgramada: String,
    val horaInicio: String,
    val idObjetivos: List<Long>,
    val idGuia: Long?,
    val notasGuia: String
)

data class CambioEstadoRequest(val estado: String, val comentario: String = "")

data class EvaluacionRequest(val calificacion: Int, val comentario: String)

// ---------------- Avisos (ms-avisos :8085) ----------------

object CategoriasAviso {
    const val CIERRE_TEMPORAL = "CIERRE_TEMPORAL"
    const val APERTURA_ESPECIAL = "APERTURA_ESPECIAL"
    const val CELEBRACION = "CELEBRACION"
    const val HORARIOS = "HORARIOS"
    const val CONSERVACION = "CONSERVACION"
    val TODAS = listOf(CIERRE_TEMPORAL, APERTURA_ESPECIAL, CELEBRACION, HORARIOS, CONSERVACION)

    fun nombreVisible(c: String) = when (c) {
        CIERRE_TEMPORAL -> "Cierre temporal"
        APERTURA_ESPECIAL -> "Apertura especial"
        CELEBRACION -> "Celebración"
        HORARIOS -> "Horarios"
        CONSERVACION -> "Conservación"
        else -> c
    }

    fun icono(c: String) = when (c) {
        CIERRE_TEMPORAL -> "⚠️"
        APERTURA_ESPECIAL -> "📢"
        CELEBRACION -> "⛪"
        HORARIOS -> "🕒"
        CONSERVACION -> "🛠️"
        else -> "🔔"
    }
}

data class Aviso(
    val idAviso: Long,
    val idNodo: Long,
    val nombreNodo: String,
    val idUsuario: Long,
    val categoria: String,
    val icono: String = "",
    val titulo: String,
    val mensaje: String,
    val fechaPublicacion: String,
    val fechaExpiracion: String,
    val haceMinutos: Long,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val estado: String,
    val vigente: Boolean,
    val cantidadReportes: Int = 0
)

data class AvisoRequest(
    val idNodo: Long,
    val nombreNodo: String,
    val idUsuario: Long,
    val categoria: String,
    val titulo: String,
    val mensaje: String,
    val horasVigencia: Int,
    val latitud: Double?,
    val longitud: Double?
)

data class ReporteRequest(val idUsuario: Long, val motivo: String)
