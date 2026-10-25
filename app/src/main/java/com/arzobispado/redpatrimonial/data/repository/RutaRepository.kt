package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.remote.RutaApi
import com.arzobispado.redpatrimonial.util.CalculadoraRuta
import java.time.LocalDate
import java.time.LocalTime

/** Repositorio de rutas, guías y salidas pedagógicas (ms-rutas). */
class RutaRepository(private val api: RutaApi) {

    // ---------------- Rutas ----------------

    suspend fun rutas(idCreador: Long?): List<Ruta> = conRespaldo(
        remoto = { api.rutas(idCreador) },
        local = { DatosPrueba.rutas.filter { !it.personalizada || (idCreador != null && it.idCreador == idCreador) } }
    )

    suspend fun ruta(id: Long): Ruta = conRespaldo(
        remoto = { api.ruta(id) },
        local = { DatosPrueba.rutas.firstOrNull { it.idRuta == id } ?: throw ErrorApi("Ruta $id no existe") }
    )

    /** CU06/CU09: crear ruta personalizada (el tiempo lo calcula el servidor). */
    suspend fun crearRuta(req: RutaRequest): Ruta = conRespaldo(
        remoto = { api.crearRuta(req) },
        local = {
            val paradas = req.paradas.mapIndexed { i, p -> p.copy(idParada = DatosPrueba.nuevoId(), ordenSecuencia = i + 1) }
            val calculo = CalculadoraRuta.calcular(paradas)
            val nueva = Ruta(DatosPrueba.nuevoId(), req.idCreador, req.nombreRuta, req.descripcion, calculo.distanciaKm,
                calculo.minutosTotal, calculo.dificultad, req.enfoque, true, paradas)
            DatosPrueba.rutas.add(nueva)
            nueva
        }
    )

    suspend fun eliminarRuta(id: Long) = conRespaldo(
        remoto = { api.eliminarRuta(id) },
        local = { DatosPrueba.rutas.removeAll { it.idRuta == id }; Unit }
    )

    // ---------------- Guías ----------------

    suspend fun guiaPorUsuario(usuario: Usuario): Guia = conRespaldo(
        remoto = { api.guiaPorUsuario(usuario.idUsuario, usuario.nombreCompleto) },
        local = {
            DatosPrueba.guias.firstOrNull { it.idUsuario == usuario.idUsuario }
                ?: Guia(DatosPrueba.nuevoId(), usuario.idUsuario, usuario.nombreCompleto, "Historia y patrimonio",
                    "Español", "Acreditación Red Patrimonial", true, 5.0, 0).also { DatosPrueba.guias.add(it) }
        }
    )

    suspend fun guiasDisponibles(fecha: String, hora: String): List<Guia> = conRespaldo(
        remoto = { api.guiasDisponibles(fecha, hora) },
        local = {
            val h = LocalTime.parse(hora)
            val libres = DatosPrueba.disponibilidad.filter {
                it.fecha == fecha && it.estadoBloque == "DISPONIBLE" &&
                    !h.isBefore(LocalTime.parse(it.horaInicio)) && h.isBefore(LocalTime.parse(it.horaFin))
            }.map { it.idGuia }.toSet()
            DatosPrueba.guias.filter { it.estadoActivo }
                .map { it.copy(disponibleEnFecha = it.idGuia in libres) }
                .sortedWith(compareByDescending<Guia> { it.disponibleEnFecha }.thenByDescending { it.calificacion })
        }
    )

    suspend fun cambiarActivo(idGuia: Long, activo: Boolean): Guia = conRespaldo(
        remoto = { api.cambiarActivo(idGuia, activo) },
        local = {
            val i = DatosPrueba.guias.indexOfFirst { it.idGuia == idGuia }
            DatosPrueba.guias[i] = DatosPrueba.guias[i].copy(estadoActivo = activo)
            DatosPrueba.guias[i]
        }
    )

    suspend fun disponibilidad(idGuia: Long): List<Disponibilidad> = conRespaldo(
        remoto = { api.disponibilidad(idGuia) },
        local = {
            val hoy = LocalDate.now().toString()
            DatosPrueba.disponibilidad.filter { it.idGuia == idGuia && it.fecha >= hoy }.sortedBy { it.fecha + it.horaInicio }
        }
    )

    /** CU16: registrar disponibilidad horaria. */
    suspend fun agregarDisponibilidad(idGuia: Long, req: DisponibilidadRequest): Disponibilidad = conRespaldo(
        remoto = { api.agregarDisponibilidad(idGuia, req) },
        local = {
            val ini = LocalTime.parse(req.horaInicio)
            val fin = LocalTime.parse(req.horaFin)
            if (LocalDate.parse(req.fecha).isBefore(LocalDate.now())) throw ErrorApi("La fecha no puede ser pasada")
            if (!fin.isAfter(ini)) throw ErrorApi("La hora de término debe ser posterior a la de inicio")
            val choca = DatosPrueba.disponibilidad.any {
                it.idGuia == idGuia && it.fecha == req.fecha &&
                    ini.isBefore(LocalTime.parse(it.horaFin)) && LocalTime.parse(it.horaInicio).isBefore(fin)
            }
            if (choca) throw ErrorApi("Ese bloque se cruza con otro ya registrado")
            Disponibilidad(DatosPrueba.nuevoId(), idGuia, req.fecha, req.horaInicio, req.horaFin, "DISPONIBLE")
                .also { DatosPrueba.disponibilidad.add(it) }
        }
    )

    suspend fun eliminarDisponibilidad(id: Long) = conRespaldo(
        remoto = { api.eliminarDisponibilidad(id) },
        local = { DatosPrueba.disponibilidad.removeAll { it.idDisponibilidad == id }; Unit }
    )

    // ---------------- Salidas pedagógicas ----------------

    suspend fun objetivos(): List<ObjetivoPedagogico> = conRespaldo(
        remoto = { api.objetivos() },
        local = { DatosPrueba.objetivos }
    )

    suspend fun salidas(idProfesor: Long? = null, idGuia: Long? = null, idNodo: Long? = null): List<SalidaPedagogica> = conRespaldo(
        remoto = { api.salidas(idProfesor, idGuia, idNodo) },
        local = {
            DatosPrueba.salidas
                .filter { idProfesor == null || it.idProfesor == idProfesor }
                .filter { idGuia == null || it.idGuia == idGuia }
                .filter { idNodo == null || idNodo in it.idNodosRuta }
                .sortedBy { it.fechaProgramada + it.horaInicio }
        }
    )

    /** CU07 + CU08: planificar salida y solicitar guía. */
    suspend fun crearSalida(req: SalidaRequest): SalidaPedagogica = conRespaldo(
        remoto = { api.crearSalida(req) },
        local = {
            if (LocalDate.parse(req.fechaProgramada).isBefore(LocalDate.now())) {
                throw ErrorApi("La salida debe programarse para hoy o una fecha futura")
            }
            val ruta = DatosPrueba.rutas.firstOrNull { it.idRuta == req.idRuta } ?: throw ErrorApi("Ruta no existe")
            val guia = DatosPrueba.guias.firstOrNull { it.idGuia == req.idGuia }
            val nueva = SalidaPedagogica(
                DatosPrueba.nuevoId(), req.idProfesor, req.nombreProfesor, req.colegio, req.curso, req.cantidadAlumnos,
                ruta.idRuta, ruta.nombreRuta, ruta.tiempoEstimadoMin, ruta.paradas.map { it.idNodo }, ruta.paradas.map { it.nombreNodo },
                req.fechaProgramada, req.horaInicio,
                DatosPrueba.objetivos.filter { it.idObjetivo in req.idObjetivos }.map { "${it.codigoCurricular} - ${it.descripcion}" },
                req.notasGuia, EstadosSalida.PENDIENTE, guia?.idGuia, guia?.nombre,
                if (guia != null) EstadosSalida.PENDIENTE else EstadosSalida.SIN_GUIA
            )
            DatosPrueba.salidas.add(nueva)
            nueva
        }
    )

    /** CU10/CU15: el encargado confirma o rechaza la visita grupal. */
    suspend fun cambiarEstadoSalida(id: Long, estado: String): SalidaPedagogica = conRespaldo(
        remoto = { api.cambiarEstadoSalida(id, CambioEstadoRequest(estado)) },
        local = {
            reemplazarSalida(id) {
                if (it.estadoSalida == EstadosSalida.FINALIZADA) throw ErrorApi("La salida ya fue finalizada")
                it.copy(estadoSalida = estado)
            }
        }
    )

    /** CU17: el guía acepta o pide reagendar. */
    suspend fun responderGuia(id: Long, estado: String, comentario: String): SalidaPedagogica = conRespaldo(
        remoto = { api.responderGuia(id, CambioEstadoRequest(estado, comentario)) },
        local = {
            reemplazarSalida(id) {
                if (it.idGuia == null) throw ErrorApi("La salida no tiene guía asignado")
                it.copy(estadoAsignacion = estado, comentarioGuia = comentario)
            }
        }
    )

    /** CU18: registrar finalización del recorrido. */
    suspend fun finalizar(id: Long, comentario: String): SalidaPedagogica = conRespaldo(
        remoto = { api.finalizar(id, CambioEstadoRequest(EstadosSalida.FINALIZADA, comentario)) },
        local = {
            reemplazarSalida(id) { s ->
                if (s.estadoAsignacion != EstadosSalida.ACEPTADA) throw ErrorApi("Solo se pueden finalizar recorridos aceptados por el guía")
                val i = DatosPrueba.guias.indexOfFirst { it.idGuia == s.idGuia }
                if (i >= 0) DatosPrueba.guias[i] = DatosPrueba.guias[i].copy(guiadosRealizados = DatosPrueba.guias[i].guiadosRealizados + 1)
                s.copy(estadoSalida = EstadosSalida.FINALIZADA, estadoAsignacion = EstadosSalida.FINALIZADA, comentarioFinal = comentario)
            }
        }
    )

    suspend fun evaluar(id: Long, nota: Int, comentario: String): SalidaPedagogica = conRespaldo(
        remoto = { api.evaluar(id, EvaluacionRequest(nota, comentario)) },
        local = {
            reemplazarSalida(id) {
                if (it.estadoSalida != EstadosSalida.FINALIZADA) throw ErrorApi("Solo se pueden evaluar salidas finalizadas")
                if (it.calificacion != null) throw ErrorApi("La salida ya fue evaluada")
                it.copy(calificacion = nota)
            }
        }
    )

    private fun reemplazarSalida(id: Long, cambio: (SalidaPedagogica) -> SalidaPedagogica): SalidaPedagogica {
        val i = DatosPrueba.salidas.indexOfFirst { it.idSalida == id }
        if (i < 0) throw ErrorApi("Salida $id no existe")
        DatosPrueba.salidas[i] = cambio(DatosPrueba.salidas[i])
        return DatosPrueba.salidas[i]
    }
}
