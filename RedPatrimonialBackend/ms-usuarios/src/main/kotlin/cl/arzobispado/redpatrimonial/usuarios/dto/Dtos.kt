package cl.arzobispado.redpatrimonial.usuarios.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Email
    val email: String = "",
    @field:NotBlank
    val password: String = ""
)

data class RegistroRequest(
    @field:NotBlank @field:Size(min = 3, max = 120)
    val nombreCompleto: String = "",
    @field:NotBlank @field:Email
    val email: String = "",
    @field:NotBlank @field:Size(min = 6, message = "debe tener al menos 6 caracteres")
    val password: String = "",
    /** TURISTA, PROFESOR o GUIA (ENCARGADO y ADMIN los asigna un administrador). */
    @field:NotBlank
    val rol: String = "TURISTA"
)

/** Lo que recibe la app: nunca incluye la contraseña. */
data class UsuarioResponse(
    val idUsuario: Long,
    val nombreCompleto: String,
    val email: String,
    val rol: String,
    val estado: String,
    val fechaRegistro: String,
    /** Solo para encargados: el templo que administran. */
    val idNodoAsignado: Long?
)

data class PermisoRequest(
    @field:Positive val idUsuario: Long = 0,
    @field:Positive val idNodo: Long = 0
)

data class PermisoResponse(
    val idPermiso: Long,
    val idUsuario: Long,
    val nombreUsuario: String,
    val idNodo: Long,
    val fechaAsignacion: String
)

data class RolResponse(val idRol: Long, val nombreRol: String, val descripcion: String)
