package com.arzobispado.redpatrimonial.data.remote

import com.arzobispado.redpatrimonial.data.model.*
import retrofit2.http.*

/** Endpoints de ms-usuarios (puerto 8081). */
interface UsuarioApi {
    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Usuario

    @POST("api/auth/registro")
    suspend fun registrar(@Body req: RegistroRequest): Usuario

    @GET("api/usuarios")
    suspend fun listar(): List<Usuario>

    @PUT("api/usuarios/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Query("estado") estado: String): Usuario

    @POST("api/permisos")
    suspend fun asignarPermiso(@Body req: PermisoRequest)
}

/** Endpoints de ms-nodos (puerto 8082). */
interface NodoApi {
    @GET("api/nodos")
    suspend fun listar(): List<NodoPatrimonial>

    @GET("api/nodos/{id}")
    suspend fun buscar(@Path("id") id: Long): NodoPatrimonial

    @PUT("api/nodos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body req: NodoRequest): NodoPatrimonial

    @PUT("api/nodos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body req: EstadoRequest): NodoPatrimonial

    @POST("api/nodos/{id}/horarios-excepcionales")
    suspend fun agregarHorario(@Path("id") id: Long, @Body req: HorarioExcepcionalRequest): HorarioExcepcional

    @DELETE("api/horarios-excepcionales/{id}")
    suspend fun eliminarHorario(@Path("id") id: Long)

    @POST("api/nodos/{id}/recursos")
    suspend fun agregarRecurso(@Path("id") id: Long, @Body req: RecursoRequest): RecursoMultimedia

    @DELETE("api/recursos/{id}")
    suspend fun eliminarRecurso(@Path("id") id: Long)
}

/** Endpoints de ms-eventos (puerto 8083). */
interface EventoApi {
    @GET("api/eventos")
    suspend fun listar(@Query("idNodo") idNodo: Long? = null): List<Evento>

    @POST("api/eventos")
    suspend fun crear(@Body req: EventoRequest): Evento

    @PUT("api/eventos/{id}/cancelar")
    suspend fun cancelar(@Path("id") id: Long): Evento

    @POST("api/eventos/{id}/inscripciones")
    suspend fun inscribir(@Path("id") idEvento: Long, @Body req: InscripcionRequest): Inscripcion

    @GET("api/inscripciones/usuario/{idUsuario}")
    suspend fun inscripciones(@Path("idUsuario") idUsuario: Long): List<Inscripcion>

    @PUT("api/inscripciones/{id}/cancelar")
    suspend fun cancelarInscripcion(@Path("id") id: Long): Inscripcion
}

/** Endpoints de ms-rutas (puerto 8084): rutas, guías y salidas pedagógicas. */
interface RutaApi {
    @GET("api/rutas")
    suspend fun rutas(@Query("idCreador") idCreador: Long? = null): List<Ruta>

    @GET("api/rutas/{id}")
    suspend fun ruta(@Path("id") id: Long): Ruta

    @POST("api/rutas")
    suspend fun crearRuta(@Body req: RutaRequest): Ruta

    @DELETE("api/rutas/{id}")
    suspend fun eliminarRuta(@Path("id") id: Long)

    @GET("api/guias/usuario/{idUsuario}")
    suspend fun guiaPorUsuario(@Path("idUsuario") idUsuario: Long, @Query("nombre") nombre: String): Guia

    @GET("api/guias/disponibles")
    suspend fun guiasDisponibles(@Query("fecha") fecha: String, @Query("hora") hora: String): List<Guia>

    @PUT("api/guias/{id}/activo")
    suspend fun cambiarActivo(@Path("id") idGuia: Long, @Query("activo") activo: Boolean): Guia

    @GET("api/guias/{id}/disponibilidad")
    suspend fun disponibilidad(@Path("id") idGuia: Long): List<Disponibilidad>

    @POST("api/guias/{id}/disponibilidad")
    suspend fun agregarDisponibilidad(@Path("id") idGuia: Long, @Body req: DisponibilidadRequest): Disponibilidad

    @DELETE("api/disponibilidad/{id}")
    suspend fun eliminarDisponibilidad(@Path("id") id: Long)

    @GET("api/objetivos")
    suspend fun objetivos(): List<ObjetivoPedagogico>

    @GET("api/salidas")
    suspend fun salidas(
        @Query("idProfesor") idProfesor: Long? = null,
        @Query("idGuia") idGuia: Long? = null,
        @Query("idNodo") idNodo: Long? = null
    ): List<SalidaPedagogica>

    @POST("api/salidas")
    suspend fun crearSalida(@Body req: SalidaRequest): SalidaPedagogica

    @PUT("api/salidas/{id}/estado")
    suspend fun cambiarEstadoSalida(@Path("id") id: Long, @Body req: CambioEstadoRequest): SalidaPedagogica

    @PUT("api/salidas/{id}/respuesta-guia")
    suspend fun responderGuia(@Path("id") id: Long, @Body req: CambioEstadoRequest): SalidaPedagogica

    @PUT("api/salidas/{id}/finalizar")
    suspend fun finalizar(@Path("id") id: Long, @Body req: CambioEstadoRequest): SalidaPedagogica

    @POST("api/salidas/{id}/evaluacion")
    suspend fun evaluar(@Path("id") id: Long, @Body req: EvaluacionRequest): SalidaPedagogica
}

/** Endpoints de ms-avisos (puerto 8085). */
interface AvisoApi {
    @GET("api/avisos")
    suspend fun listar(@Query("soloVigentes") soloVigentes: Boolean = true): List<Aviso>

    @POST("api/avisos")
    suspend fun publicar(@Body req: AvisoRequest): Aviso

    @PUT("api/avisos/{id}/retirar")
    suspend fun retirar(@Path("id") id: Long): Aviso

    @POST("api/avisos/{id}/reportes")
    suspend fun reportar(@Path("id") id: Long, @Body req: ReporteRequest)
}
