package com.arzobispado.redpatrimonial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base de todos los ViewModels: maneja el indicador de carga y los mensajes
 * (éxito o error) que las pantallas muestran en un Snackbar.
 */
abstract class BaseViewModel : ViewModel() {

    protected val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    protected val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /** Ejecuta una tarea en segundo plano mostrando "cargando" y capturando errores. */
    protected fun ejecutar(mensajeExito: String? = null, alTerminar: () -> Unit = {}, tarea: suspend () -> Unit) {
        // UNDISPATCHED: la tarea parte de inmediato (también permite que las vistas previas carguen datos)
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            _cargando.value = true
            try {
                tarea()
                if (mensajeExito != null) _mensaje.value = mensajeExito
                alTerminar()
            } catch (e: Exception) {
                _mensaje.value = e.message ?: "Ocurrió un error inesperado"
            } finally {
                _cargando.value = false
            }
        }
    }
}
