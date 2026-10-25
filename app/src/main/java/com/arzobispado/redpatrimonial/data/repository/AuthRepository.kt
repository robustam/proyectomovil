package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.remote.UsuarioApi

/** Repositorio de usuarios: la única puerta de entrada a los datos de ms-usuarios. */
class AuthRepository(private val api: UsuarioApi) {

    suspend fun login(email: String, clave: String): Usuario = conRespaldo(
        remoto = { api.login(LoginRequest(email.trim(), clave)) },
        local = {
            val usuario = DatosPrueba.usuarios.firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            val claveCorrecta = DatosPrueba.claves[usuario?.email] ?: DatosPrueba.CLAVE_DEMO
            if (usuario == null || clave != claveCorrecta) throw ErrorApi("Correo o contraseña incorrectos")
            if (usuario.estado != "ACTIVO") throw ErrorApi("La cuenta está desactivada")
            usuario
        }
    )

    suspend fun registrar(req: RegistroRequest): Usuario = conRespaldo(
        remoto = { api.registrar(req) },
        local = {
            if (DatosPrueba.usuarios.any { it.email.equals(req.email, true) }) {
                throw ErrorApi("Ya existe una cuenta con el correo ${req.email}")
            }
            val nuevo = Usuario(DatosPrueba.nuevoId(), req.nombreCompleto, req.email.lowercase(), req.rol)
            DatosPrueba.usuarios.add(nuevo)
            DatosPrueba.claves[nuevo.email] = req.password
            nuevo
        }
    )

    suspend fun listarUsuarios(): List<Usuario> = conRespaldo(
        remoto = { api.listar() },
        local = { DatosPrueba.usuarios.toList() }
    )

    suspend fun cambiarEstado(id: Long, estado: String): Usuario = conRespaldo(
        remoto = { api.cambiarEstado(id, estado) },
        local = {
            val i = DatosPrueba.usuarios.indexOfFirst { it.idUsuario == id }
            if (i < 0) throw ErrorApi("Usuario no existe")
            DatosPrueba.usuarios[i] = DatosPrueba.usuarios[i].copy(estado = estado)
            DatosPrueba.usuarios[i]
        }
    )

    suspend fun asignarNodo(idUsuario: Long, idNodo: Long) = conRespaldo(
        remoto = { api.asignarPermiso(PermisoRequest(idUsuario, idNodo)) },
        local = {
            val i = DatosPrueba.usuarios.indexOfFirst { it.idUsuario == idUsuario }
            if (i < 0 || DatosPrueba.usuarios[i].rol != Roles.ENCARGADO) {
                throw ErrorApi("Solo se pueden asignar nodos a usuarios con rol ENCARGADO")
            }
            DatosPrueba.usuarios[i] = DatosPrueba.usuarios[i].copy(idNodoAsignado = idNodo)
            Unit
        }
    )
}
