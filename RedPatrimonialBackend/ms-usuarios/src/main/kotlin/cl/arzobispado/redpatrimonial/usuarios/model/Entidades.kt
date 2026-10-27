package cl.arzobispado.redpatrimonial.usuarios.model

import jakarta.persistence.*
import java.time.LocalDateTime

/** Tabla ROL: perfiles de la red (TURISTA, PROFESOR, GUIA, ENCARGADO, ADMIN). */
@Entity
@Table(name = "rol")
class Rol(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    var idRol: Long? = null,

    @Column(name = "nombre_rol", length = 50, unique = true, nullable = false)
    var nombreRol: String = "",

    @Column(length = 150)
    var descripcion: String = ""
)

/** Tabla USUARIO. La contraseña se guarda cifrada con BCrypt (nunca en texto plano). */
@Entity
@Table(name = "usuario")
class Usuario(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    var idUsuario: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol")
    var rol: Rol = Rol(),

    @Column(name = "nombre_completo", length = 120, nullable = false)
    var nombreCompleto: String = "",

    @Column(length = 100, unique = true, nullable = false)
    var email: String = "",

    @Column(name = "password_hash", length = 255, nullable = false)
    var passwordHash: String = "",

    @Column(name = "fecha_registro")
    var fechaRegistro: LocalDateTime = LocalDateTime.now(),

    @Column(length = 30)
    var estado: String = "ACTIVO"
)

/**
 * Tabla PERMISO_NODO: qué templo/nodo administra cada encargado.
 * id_nodo pertenece al microservicio ms-nodos, por eso se guarda solo el id
 * (cada microservicio es dueño de sus propios datos).
 */
@Entity
@Table(name = "permiso_nodo")
class PermisoNodo(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    var idPermiso: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario")
    var usuario: Usuario = Usuario(),

    @Column(name = "id_nodo", nullable = false)
    var idNodo: Long = 0,

    @Column(name = "fecha_asignacion")
    var fechaAsignacion: LocalDateTime = LocalDateTime.now()
)
