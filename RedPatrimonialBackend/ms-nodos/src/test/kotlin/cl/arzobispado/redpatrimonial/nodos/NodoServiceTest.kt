package cl.arzobispado.redpatrimonial.nodos

import cl.arzobispado.redpatrimonial.nodos.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.nodos.dto.HorarioExcepcionalRequest
import cl.arzobispado.redpatrimonial.nodos.model.EstadoApertura
import cl.arzobispado.redpatrimonial.nodos.model.HorarioExcepcional
import cl.arzobispado.redpatrimonial.nodos.model.NodoPatrimonial
import cl.arzobispado.redpatrimonial.nodos.repository.*
import cl.arzobispado.redpatrimonial.nodos.service.NodoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Optional

/** Pruebas de la lógica de estado en tiempo real y horarios excepcionales. */
class NodoServiceTest {

    private val nodoRepo = mock(NodoRepository::class.java)
    private val service = NodoService(
        nodoRepo,
        mock(ComunaRepository::class.java),
        mock(TipoNodoRepository::class.java),
        mock(DecanatoRepository::class.java),
        mock(HorarioExcepcionalRepository::class.java),
        mock(RecursoRepository::class.java)
    )

    @Test
    fun `sin horario excepcional se usa el estado del encargado`() {
        val nodo = NodoPatrimonial(idNodo = 1, estadoOperativo = EstadoApertura.CERRADO)
        assertEquals(EstadoApertura.CERRADO, service.calcularEstadoActual(nodo, LocalDateTime.now()))
    }

    @Test
    fun `un horario excepcional vigente cambia el estado actual`() {
        val hoy = LocalDate.of(2026, 10, 24)
        val nodo = NodoPatrimonial(idNodo = 1, estadoOperativo = EstadoApertura.CERRADO)
        nodo.horariosExcepcionales.add(
            HorarioExcepcional(nodo = nodo, fecha = hoy, horaInicio = LocalTime.of(10, 0),
                horaFin = LocalTime.of(13, 0), estadoApertura = EstadoApertura.ABIERTO)
        )
        assertEquals(EstadoApertura.ABIERTO, service.calcularEstadoActual(nodo, hoy.atTime(11, 0)))
        assertEquals(EstadoApertura.CERRADO, service.calcularEstadoActual(nodo, hoy.atTime(14, 0)))
    }

    @Test
    fun `rechaza horario con hora de termino anterior al inicio`() {
        `when`(nodoRepo.findById(1L)).thenReturn(Optional.of(NodoPatrimonial(idNodo = 1)))
        val req = HorarioExcepcionalRequest(LocalDate.now().plusDays(1).toString(), "14:00", "10:00", "ABIERTO", "")
        assertThrows<ReglaNegocioException> { service.agregarHorarioExcepcional(1, req) }
    }

    @Test
    fun `rechaza estados que no existen`() {
        `when`(nodoRepo.findById(1L)).thenReturn(Optional.of(NodoPatrimonial(idNodo = 1)))
        assertThrows<ReglaNegocioException> { service.cambiarEstado(1, "VACACIONES") }
    }
}
