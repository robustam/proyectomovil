package cl.arzobispado.redpatrimonial.eventos.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "categoria_evento")
class CategoriaEvento(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_ev") var idCategoriaEv: Long? = null,
    @Column(name = "nombre_categoria", length = 80, unique = true) var nombreCategoria: String = "",
    @Column(length = 255) var descripcion: String = ""
)

/**
 * Tabla EVENTO_CULTURAL. id_nodo pertenece a ms-nodos; se guarda una copia del
 * nombre y las coordenadas para poder indexar los eventos por ubicación sin
 * depender de otro microservicio en cada consulta.
 */
@Entity
@Table(name = "evento_cultural")
class EventoCultural(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento") var idEvento: Long? = null,
    @Column(name = "id_nodo") var idNodo: Long = 0,
    @Column(name = "nombre_nodo", length = 150) var nombreNodo: String = "",
    var latitud: Double? = null,
    var longitud: Double? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_categoria_ev") var categoria: CategoriaEvento? = null,
    @Column(length = 150) var titulo: String = "",
    @Column(columnDefinition = "TEXT") var descripcion: String = "",
    @Column(name = "fecha_inicio") var fechaInicio: LocalDateTime = LocalDateTime.now(),
    @Column(name = "fecha_fin") var fechaFin: LocalDateTime = LocalDateTime.now().plusHours(1),
    @Column(name = "cupo_maximo") var cupoMaximo: Int = 0,
    @Column(name = "requiere_inscripcion") var requiereInscripcion: Boolean = false,
    /** PROGRAMADO, CANCELADO o FINALIZADO */
    @Column(length = 30) var estado: String = "PROGRAMADO",
    @Column(name = "id_publicador") var idPublicador: Long? = null
)

/** Tabla INSCRIPCION_EVENTO: la reserva de cupo con su código QR (CU05). */
@Entity
@Table(name = "inscripcion_evento")
class InscripcionEvento(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion") var idInscripcion: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "id_evento") var evento: EventoCultural = EventoCultural(),
    @Column(name = "id_usuario") var idUsuario: Long = 0,
    @Column(name = "cantidad_personas") var cantidadPersonas: Int = 1,
    @Column(name = "fecha_inscripcion") var fechaInscripcion: LocalDateTime = LocalDateTime.now(),
    /** CONFIRMADA, CANCELADA o ASISTIO */
    @Column(name = "estado_asistencia", length = 30) var estadoAsistencia: String = "CONFIRMADA",
    @Column(name = "codigo_qr", length = 100, unique = true) var codigoQr: String = ""
)
