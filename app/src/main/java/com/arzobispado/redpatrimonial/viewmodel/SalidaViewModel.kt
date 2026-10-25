package com.arzobispado.redpatrimonial.viewmodel

import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.repository.AppContainer
import com.arzobispado.redpatrimonial.data.repository.RutaRepository
import com.arzobispado.redpatrimonial.util.Formatos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/** Datos que el profesor va completando en el asistente de 3 pasos. */
data class FormularioSalida(
    val colegio: String = "",
    val curso: String = "",
    val cantidadAlumnos: String = "",
    val fecha: String = LocalDate.now().plusDays(7).toString(),
    val hora: String = "10:00",
    val idRuta: Long? = null,
    val objetivos: Set<Long> = emptySet(),
    val idGuia: Long? = null,
    val notasGuia: String = ""
)

/**
 * Salidas pedagógicas: lo usan el profesor (planificar, CU07-CU08),
 * el encargado de templo (confirmar visitas, CU10/CU15) y el docente al evaluar.
 */
class SalidaViewModel(
    private val repo: RutaRepository = AppContainer.rutaRepository
) : BaseViewModel() {

    private val _salidas = MutableStateFlow<List<SalidaPedagogica>>(emptyList())
    val salidas: StateFlow<List<SalidaPedagogica>> = _salidas.asStateFlow()

    private val _objetivos = MutableStateFlow<List<ObjetivoPedagogico>>(emptyList())
    val objetivos: StateFlow<List<ObjetivoPedagogico>> = _objetivos.asStateFlow()

    private val _guias = MutableStateFlow<List<Guia>>(emptyList())
    val guias: StateFlow<List<Guia>> = _guias.asStateFlow()

    private val _formulario = MutableStateFlow(FormularioSalida())
    val formulario: StateFlow<FormularioSalida> = _formulario.asStateFlow()

    private val _paso = MutableStateFlow(1)
    val paso: StateFlow<Int> = _paso.asStateFlow()

    fun cargarDeProfesor(idProfesor: Long) = ejecutar { _salidas.value = repo.salidas(idProfesor = idProfesor) }

    /** Solicitudes de visita que incluyen el templo del encargado. */
    fun cargarDeNodo(idNodo: Long) = ejecutar { _salidas.value = repo.salidas(idNodo = idNodo) }

    // ----- Asistente de 3 pasos -----

    fun iniciarFormulario() {
        _formulario.value = FormularioSalida()
        _paso.value = 1
        ejecutar { _objetivos.value = repo.objetivos() }
    }

    fun actualizar(f: FormularioSalida) {
        _formulario.value = f
    }

    fun alternarObjetivo(id: Long) {
        val f = _formulario.value
        _formulario.value = f.copy(objetivos = if (id in f.objetivos) f.objetivos - id else f.objetivos + id)
    }

    fun siguientePaso() {
        val error = validarPaso(_paso.value, _formulario.value)
        if (error != null) {
            _mensaje.value = error
            return
        }
        if (_paso.value == 2) {
            val f = _formulario.value
            ejecutar { _guias.value = repo.guiasDisponibles(f.fecha, f.hora) }
        }
        _paso.value = (_paso.value + 1).coerceAtMost(3)
    }

    fun pasoAnterior() {
        _paso.value = (_paso.value - 1).coerceAtLeast(1)
    }

    fun enviar(profesor: Usuario, alEnviar: () -> Unit) {
        val f = _formulario.value
        ejecutar(mensajeExito = "Solicitud enviada. El templo y el guía deben confirmarla", alTerminar = alEnviar) {
            repo.crearSalida(
                SalidaRequest(
                    idProfesor = profesor.idUsuario, nombreProfesor = profesor.nombreCompleto,
                    colegio = f.colegio.trim(), curso = f.curso.trim(), cantidadAlumnos = f.cantidadAlumnos.toInt(),
                    idRuta = f.idRuta!!, fechaProgramada = f.fecha, horaInicio = f.hora,
                    idObjetivos = f.objetivos.toList(), idGuia = f.idGuia, notasGuia = f.notasGuia.trim()
                )
            )
            _salidas.value = repo.salidas(idProfesor = profesor.idUsuario)
        }
    }

    // ----- Encargado de templo -----

    fun responderSolicitud(salida: SalidaPedagogica, confirmar: Boolean, idNodo: Long) =
        ejecutar(mensajeExito = if (confirmar) "Visita confirmada" else "Visita rechazada") {
            repo.cambiarEstadoSalida(salida.idSalida, if (confirmar) EstadosSalida.CONFIRMADA else EstadosSalida.RECHAZADA)
            _salidas.value = repo.salidas(idNodo = idNodo)
        }

    // ----- Evaluación del docente -----

    fun evaluar(salida: SalidaPedagogica, nota: Int, comentario: String) = ejecutar(mensajeExito = "¡Gracias por evaluar la salida!") {
        repo.evaluar(salida.idSalida, nota, comentario)
        _salidas.value = repo.salidas(idProfesor = salida.idProfesor)
    }

    companion object {
        fun validarPaso(paso: Int, f: FormularioSalida): String? = when (paso) {
            1 -> when {
                f.colegio.isBlank() -> "Ingresa el nombre del colegio"
                f.curso.isBlank() -> "Ingresa el curso"
                (f.cantidadAlumnos.toIntOrNull() ?: 0) !in 1..200 -> "La cantidad de estudiantes debe estar entre 1 y 200"
                !Formatos.esFechaValida(f.fecha) -> "La fecha debe tener formato AAAA-MM-DD"
                LocalDate.parse(f.fecha).isBefore(LocalDate.now()) -> "La fecha no puede ser pasada"
                !Formatos.esHoraValida(f.hora) -> "La hora debe tener formato HH:MM"
                else -> null
            }
            2 -> when {
                f.idRuta == null -> "Selecciona una ruta"
                f.objetivos.isEmpty() -> "Selecciona al menos un objetivo pedagógico"
                else -> null
            }
            else -> null
        }
    }
}
