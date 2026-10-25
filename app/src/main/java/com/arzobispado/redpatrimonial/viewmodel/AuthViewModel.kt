package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.RegistroRequest
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.data.model.Usuario
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sesión del usuario. Se comparte entre todas las pantallas:
 * usuario == null significa "modo invitado".
 */
class AuthViewModel(
    private val repo: AuthRepository = AppContainer.authRepository
) : BaseViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    /** Rol actual (los invitados navegan como turistas). */
    val rolActual: String get() = _usuario.value?.rol ?: Roles.TURISTA
    val esInvitado: Boolean get() = _usuario.value == null

    // ----- Administración (CU20) -----
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    fun login(correo: String, clave: String, alIngresar: (Usuario) -> Unit) {
        val error = validarLogin(correo, clave)
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar {
            val u = repo.login(correo, clave)
            _usuario.value = u
            alIngresar(u)
        }
    }

    fun registrar(nombre: String, correo: String, clave: String, confirmar: String, rol: String, alRegistrar: (Usuario) -> Unit) {
        val error = validarRegistro(nombre, correo, clave, confirmar)
        if (error != null) {
            _mensaje.value = error
            return
        }
        ejecutar(mensajeExito = "¡Cuenta creada! Bienvenido/a a la Red Patrimonial") {
            val u = repo.registrar(RegistroRequest(nombre.trim(), correo.trim(), clave, rol))
            _usuario.value = u
            alRegistrar(u)
        }
    }

    fun entrarComoInvitado() {
        _usuario.value = null
    }

    fun logout() {
        _usuario.value = null
        _mensaje.value = null
    }

    /** Recuperación simulada: en el MVP no se envían correos reales. */
    fun recuperarClave(correo: String, alEnviar: () -> Unit) {
        if (!esCorreoValido(correo)) {
            _mensaje.value = "Ingresa un correo válido"
            return
        }
        _mensaje.value = "Si el correo existe, recibirás instrucciones (simulado)"
        alEnviar()
    }

    fun cargarUsuarios() = ejecutar { _usuarios.value = repo.listarUsuarios() }

    fun cambiarEstadoUsuario(u: Usuario) = ejecutar(mensajeExito = "Estado actualizado") {
        repo.cambiarEstado(u.idUsuario, if (u.estado == "ACTIVO") "INACTIVO" else "ACTIVO")
        _usuarios.value = repo.listarUsuarios()
    }

    fun asignarNodo(idUsuario: Long, idNodo: Long) = ejecutar(mensajeExito = "Templo asignado al encargado") {
        repo.asignarNodo(idUsuario, idNodo)
        _usuarios.value = repo.listarUsuarios()
    }

    companion object {
        fun esCorreoValido(correo: String) = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$").matches(correo.trim())

        fun validarLogin(correo: String, clave: String): String? = when {
            correo.isBlank() || clave.isBlank() -> "Completa el correo y la contraseña"
            !esCorreoValido(correo) -> "El correo no tiene un formato válido"
            else -> null
        }

        fun validarRegistro(nombre: String, correo: String, clave: String, confirmar: String): String? = when {
            nombre.trim().length < 3 -> "Ingresa tu nombre completo"
            !esCorreoValido(correo) -> "El correo no tiene un formato válido"
            clave.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            clave != confirmar -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}
