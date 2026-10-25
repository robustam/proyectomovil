package com.arzobispado.redpatrimonial.data.repository

import com.arzobispado.redpatrimonial.data.mock.DatosPrueba
import com.arzobispado.redpatrimonial.data.model.*
import com.arzobispado.redpatrimonial.data.remote.AvisoApi
import java.time.LocalDateTime

/** Repositorio de avisos comunitarios en tiempo real (ms-avisos). */
class AvisoRepository(private val api: AvisoApi) {

    suspend fun listar(): List<Aviso> = conRespaldo(
        remoto = { api.listar(true) },
        local = {
            val ahora = LocalDateTime.now().toString()
            DatosPrueba.avisos.filter { it.estado == "ACTIVO" && it.fechaExpiracion > ahora }
                .sortedBy { it.haceMinutos }
        }
    )

    /** CU14: publicar aviso / alerta. */
    suspend fun publicar(req: AvisoRequest): Aviso = conRespaldo(
        remoto = { api.publicar(req) },
        local = {
            val ahora = LocalDateTime.now()
            Aviso(DatosPrueba.nuevoId(), req.idNodo, req.nombreNodo, req.idUsuario, req.categoria,
                CategoriasAviso.icono(req.categoria), req.titulo, req.mensaje, ahora.toString(),
                ahora.plusHours(req.horasVigencia.toLong()).toString(), 0, req.latitud, req.longitud, "ACTIVO", true)
                .also { DatosPrueba.avisos.add(0, it) }
        }
    )

    suspend fun retirar(id: Long): Aviso = conRespaldo(
        remoto = { api.retirar(id) },
        local = {
            val i = DatosPrueba.avisos.indexOfFirst { it.idAviso == id }
            if (i < 0) throw ErrorApi("Aviso no existe")
            DatosPrueba.avisos[i] = DatosPrueba.avisos[i].copy(estado = "RETIRADO", vigente = false)
            DatosPrueba.avisos[i]
        }
    )

    suspend fun reportar(id: Long, idUsuario: Long, motivo: String) = conRespaldo(
        remoto = { api.reportar(id, ReporteRequest(idUsuario, motivo)) },
        local = {
            val i = DatosPrueba.avisos.indexOfFirst { it.idAviso == id }
            if (i >= 0) DatosPrueba.avisos[i] = DatosPrueba.avisos[i].copy(cantidadReportes = DatosPrueba.avisos[i].cantidadReportes + 1)
            Unit
        }
    )
}
