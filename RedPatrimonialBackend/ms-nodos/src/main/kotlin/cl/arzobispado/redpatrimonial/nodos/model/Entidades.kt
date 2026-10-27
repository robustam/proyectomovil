package cl.arzobispado.redpatrimonial.nodos.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "region")
class Region(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region") var idRegion: Long? = null,
    @Column(name = "nombre_region", length = 100) var nombreRegion: String = ""
)

@Entity
@Table(name = "comuna")
class Comuna(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comuna") var idComuna: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_region") var region: Region? = null,
    @Column(name = "nombre_comuna", length = 100) var nombreComuna: String = ""
)

@Entity
@Table(name = "zona_pastoral")
class ZonaPastoral(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona") var idZona: Long? = null,
    @Column(name = "nombre_zona", length = 100) var nombreZona: String = "",
    @Column(columnDefinition = "TEXT") var descripcion: String = ""
)

@Entity
@Table(name = "decanato")
class Decanato(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decanato") var idDecanato: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_zona") var zona: ZonaPastoral? = null,
    @Column(name = "nombre_decanato", length = 100) var nombreDecanato: String = ""
)

/** TEMPLO, MUSEO, COLEGIO, ARCHIVO */
@Entity
@Table(name = "tipo_nodo")
class TipoNodo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_nodo") var idTipoNodo: Long? = null,
    @Column(name = "nombre_tipo", length = 50, unique = true) var nombreTipo: String = ""
)

/**
 * Estados de apertura definidos en el anexo del Arzobispado (punto 3.2):
 * Abierto, Cerrado y En Celebración.
 */
enum class EstadoApertura { ABIERTO, CERRADO, EN_CELEBRACION }

/** Tabla NODO_PATRIMONIAL: la ficha de cada templo, museo, colegio o archivo. */
@Entity
@Table(name = "nodo_patrimonial")
class NodoPatrimonial(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nodo") var idNodo: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_tipo_nodo")
    var tipoNodo: TipoNodo? = null,

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_decanato")
    var decanato: Decanato? = null,

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_comuna")
    var comuna: Comuna? = null,

    @Column(name = "nombre_oficial", length = 150) var nombreOficial: String = "",
    @Column(length = 200) var direccion: String = "",
    @Column(precision = 10, scale = 6) var latitud: BigDecimal = BigDecimal.ZERO,
    @Column(precision = 11, scale = 6) var longitud: BigDecimal = BigDecimal.ZERO,
    @Column(name = "resena_historica", columnDefinition = "TEXT") var resenaHistorica: String = "",
    @Column(name = "estilo_arquitectonico", length = 100) var estiloArquitectonico: String = "",
    @Column(name = "fecha_construccion") var fechaConstruccion: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", length = 30)
    var estadoOperativo: EstadoApertura = EstadoApertura.ABIERTO,

    @OneToMany(mappedBy = "nodo", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("diaSemana ASC")
    var horariosRegulares: MutableList<HorarioRegular> = mutableListOf(),

    @OneToMany(mappedBy = "nodo", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fecha ASC, horaInicio ASC")
    var horariosExcepcionales: MutableList<HorarioExcepcional> = mutableListOf(),

    @OneToMany(mappedBy = "nodo", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var recursos: MutableList<RecursoMultimedia> = mutableListOf()
)

/** Horario normal de cada día (1 = lunes ... 7 = domingo). */
@Entity
@Table(name = "horario_regular")
class HorarioRegular(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario") var idHorario: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_nodo") var nodo: NodoPatrimonial? = null,
    @Column(name = "dia_semana") var diaSemana: Int = 1,
    @Column(name = "hora_apertura") var horaApertura: LocalTime = LocalTime.of(9, 0),
    @Column(name = "hora_cierre") var horaCierre: LocalTime = LocalTime.of(19, 0)
)

/**
 * Bloques especiales: un templo cerrado que abre de forma excepcional,
 * un cierre por celebración, etc. (CU11).
 */
@Entity
@Table(name = "horario_excepcional")
class HorarioExcepcional(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario_exc") var idHorarioExc: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_nodo") var nodo: NodoPatrimonial? = null,
    var fecha: LocalDate = LocalDate.now(),
    @Column(name = "hora_inicio") var horaInicio: LocalTime = LocalTime.of(9, 0),
    @Column(name = "hora_fin") var horaFin: LocalTime = LocalTime.of(18, 0),
    @Column(length = 200) var motivo: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_apertura", length = 30)
    var estadoApertura: EstadoApertura = EstadoApertura.ABIERTO
)

/** Galería multimedia simulada (IMAGEN, AUDIO para audioguías, VIDEO). */
@Entity
@Table(name = "recurso_multimedia")
class RecursoMultimedia(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recurso") var idRecurso: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_nodo") var nodo: NodoPatrimonial? = null,
    @Column(name = "tipo_recurso", length = 30) var tipoRecurso: String = "IMAGEN",
    @Column(name = "url_recurso", length = 255) var urlRecurso: String = "",
    @Column(length = 100) var titulo: String = "",
    @Column(name = "duracion_segundos") var duracionSegundos: Int? = null,
    @Column(name = "es_principal") var esPrincipal: Boolean = false
)
