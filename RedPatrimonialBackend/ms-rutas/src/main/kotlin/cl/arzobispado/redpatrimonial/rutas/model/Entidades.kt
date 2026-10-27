package cl.arzobispado.redpatrimonial.rutas.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Tabla RUTA_PATRIMONIAL: rutas recomendadas y rutas personalizadas de los usuarios. */
@Entity
@Table(name = "ruta_patrimonial")
class RutaPatrimonial(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta") var idRuta: Long? = null,
    /** null = ruta oficial recomendada por la red */
    @Column(name = "id_creador") var idCreador: Long? = null,
    @Column(name = "nombre_ruta", length = 120) var nombreRuta: String = "",
    @Column(columnDefinition = "TEXT") var descripcion: String = "",
    @Column(name = "distancia_km") var distanciaKm: Double = 0.0,
    @Column(name = "tiempo_estimado_min") var tiempoEstimadoMin: Int = 0,
    @Column(name = "nivel_dificultad", length = 30) var nivelDificultad: String = "Baja",
    @Column(length = 50) var enfoque: String = "",
    var personalizada: Boolean = false,
    @OneToMany(mappedBy = "ruta", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordenSecuencia ASC")
    var paradas: MutableList<ParadaRuta> = mutableListOf()
)

/** Tabla PARADA_RUTA. Guarda copia del nombre y coordenadas del nodo (dueño: ms-nodos). */
@Entity
@Table(name = "parada_ruta")
class ParadaRuta(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parada") var idParada: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_ruta") var ruta: RutaPatrimonial? = null,
    @Column(name = "id_nodo") var idNodo: Long = 0,
    @Column(name = "nombre_nodo", length = 150) var nombreNodo: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    @Column(name = "orden_secuencia") var ordenSecuencia: Int = 1,
    @Column(name = "tiempo_sugerido_min") var tiempoSugeridoMin: Int = 30,
    @Column(name = "notas_recorrido", columnDefinition = "TEXT") var notasRecorrido: String = ""
)

/** Tabla GUIA_VOLUNTARIO: perfil del guía (id_usuario pertenece a ms-usuarios). */
@Entity
@Table(name = "guia_voluntario")
class GuiaVoluntario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guia") var idGuia: Long? = null,
    @Column(name = "id_usuario", unique = true) var idUsuario: Long = 0,
    @Column(length = 120) var nombre: String = "",
    @Column(length = 100) var especialidad: String = "Historia y patrimonio",
    @Column(length = 100) var idiomas: String = "Español",
    @Column(length = 100) var certificacion: String = "Acreditación Red Patrimonial",
    @Column(name = "estado_activo") var estadoActivo: Boolean = true,
    var calificacion: Double = 5.0,
    @Column(name = "cantidad_evaluaciones") var cantidadEvaluaciones: Int = 0,
    @Column(name = "guiados_realizados") var guiadosRealizados: Int = 0
)

/** Tabla DISPONIBILIDAD_GUIA: bloques en que el guía puede acompañar grupos (CU16). */
@Entity
@Table(name = "disponibilidad_guia")
class DisponibilidadGuia(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad") var idDisponibilidad: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_guia") var guia: GuiaVoluntario? = null,
    var fecha: LocalDate = LocalDate.now(),
    @Column(name = "hora_inicio") var horaInicio: LocalTime = LocalTime.of(9, 0),
    @Column(name = "hora_fin") var horaFin: LocalTime = LocalTime.of(13, 0),
    /** DISPONIBLE u OCUPADO */
    @Column(name = "estado_bloque", length = 30) var estadoBloque: String = "DISPONIBLE"
)

/** Tabla OBJETIVO_PEDAGOGICO (códigos curriculares de ejemplo). */
@Entity
@Table(name = "objetivo_pedagogico")
class ObjetivoPedagogico(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_objetivo") var idObjetivo: Long? = null,
    @Column(name = "codigo_curricular", length = 30) var codigoCurricular: String = "",
    @Column(columnDefinition = "TEXT") var descripcion: String = "",
    @Column(length = 80) var asignatura: String = ""
)

/** Tabla SALIDA_PEDAGOGICA (CU07, CU08, CU10). */
@Entity
@Table(name = "salida_pedagogica")
class SalidaPedagogica(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salida") var idSalida: Long? = null,
    @Column(name = "id_profesor_cargo") var idProfesor: Long = 0,
    @Column(name = "nombre_profesor", length = 120) var nombreProfesor: String = "",
    @Column(length = 150) var colegio: String = "",
    @Column(length = 50) var curso: String = "",
    @Column(name = "cantidad_alumnos") var cantidadAlumnos: Int = 0,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_ruta") var ruta: RutaPatrimonial? = null,
    @Column(name = "fecha_programada") var fechaProgramada: LocalDate = LocalDate.now(),
    @Column(name = "hora_inicio") var horaInicio: LocalTime = LocalTime.of(10, 0),
    /** PENDIENTE, CONFIRMADA, RECHAZADA o FINALIZADA */
    @Column(name = "estado_salida", length = 30) var estadoSalida: String = "PENDIENTE",
    @Column(name = "notas_guia", columnDefinition = "TEXT") var notasGuia: String = "",
    @Column(name = "comentario_final", columnDefinition = "TEXT") var comentarioFinal: String = "",
    /** Tabla DETALLE_OBJETIVO_SALIDA */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "detalle_objetivo_salida",
        joinColumns = [JoinColumn(name = "id_salida")],
        inverseJoinColumns = [JoinColumn(name = "id_objetivo")]
    )
    var objetivos: MutableSet<ObjetivoPedagogico> = mutableSetOf()
)

/** Tabla ASIGNACION_GUIA (CU08, CU17, CU18). */
@Entity
@Table(name = "asignacion_guia")
class AsignacionGuia(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion") var idAsignacion: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_guia") var guia: GuiaVoluntario? = null,
    @OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_salida", unique = true) var salida: SalidaPedagogica? = null,
    @Column(name = "fecha_asignacion") var fechaAsignacion: LocalDateTime = LocalDateTime.now(),
    /** PENDIENTE, ACEPTADA, REAGENDAR o FINALIZADA */
    @Column(name = "estado_confirmacion", length = 30) var estadoConfirmacion: String = "PENDIENTE",
    @Column(name = "comentario_guia", length = 255) var comentarioGuia: String = ""
)

/** Tabla EVALUACION_SALIDA: el docente califica la experiencia. */
@Entity
@Table(name = "evaluacion_salida")
class EvaluacionSalida(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion") var idEvaluacion: Long? = null,
    @OneToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_salida", unique = true) var salida: SalidaPedagogica? = null,
    @Column(name = "calificacion_docente") var calificacionDocente: Int = 5,
    @Column(name = "comentarios_pedagogicos", columnDefinition = "TEXT") var comentarios: String = "",
    @Column(name = "fecha_registro") var fechaRegistro: LocalDateTime = LocalDateTime.now()
)
