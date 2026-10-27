package cl.arzobispado.redpatrimonial.avisos.model

import jakarta.persistence.*
import java.time.LocalDateTime

/** CIERRE_TEMPORAL, APERTURA_ESPECIAL, CELEBRACION, HORARIOS, CONSERVACION */
@Entity
@Table(name = "categoria_aviso")
class CategoriaAviso(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cat_aviso") var idCatAviso: Long? = null,
    @Column(name = "nombre_cat", length = 50, unique = true) var nombreCat: String = "",
    @Column(length = 50) var icono: String = ""
)

/** Tabla AVISO_COMUNITARIO: publicaciones en tiempo real de los templos (modelo tipo SoSafe). */
@Entity
@Table(name = "aviso_comunitario")
class AvisoComunitario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aviso") var idAviso: Long? = null,
    @Column(name = "id_nodo") var idNodo: Long = 0,
    @Column(name = "nombre_nodo", length = 150) var nombreNodo: String = "",
    @Column(name = "id_usuario") var idUsuario: Long = 0,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_cat_aviso") var categoria: CategoriaAviso? = null,
    @Column(length = 100) var titulo: String = "",
    @Column(columnDefinition = "TEXT") var mensaje: String = "",
    @Column(name = "fecha_publicacion") var fechaPublicacion: LocalDateTime = LocalDateTime.now(),
    @Column(name = "fecha_expiracion") var fechaExpiracion: LocalDateTime = LocalDateTime.now().plusHours(24),
    var latitud: Double? = null,
    var longitud: Double? = null,
    /** ACTIVO, RETIRADO o EN_REVISION (muchos reportes) */
    @Column(length = 30) var estado: String = "ACTIVO"
)

/** Tabla REPORTE_AVISO: un usuario reporta un aviso incorrecto o inapropiado. */
@Entity
@Table(name = "reporte_aviso")
class ReporteAviso(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte") var idReporte: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "id_aviso") var aviso: AvisoComunitario? = null,
    @Column(name = "id_usuario_reporta") var idUsuarioReporta: Long = 0,
    @Column(length = 200) var motivo: String = "",
    @Column(name = "fecha_reporte") var fechaReporte: LocalDateTime = LocalDateTime.now()
)
