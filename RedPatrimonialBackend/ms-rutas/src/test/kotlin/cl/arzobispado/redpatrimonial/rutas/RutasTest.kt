package cl.arzobispado.redpatrimonial.rutas

import cl.arzobispado.redpatrimonial.rutas.config.ReglaNegocioException
import cl.arzobispado.redpatrimonial.rutas.dto.CambioEstadoRequest
import cl.arzobispado.redpatrimonial.rutas.model.AsignacionGuia
import cl.arzobispado.redpatrimonial.rutas.model.SalidaPedagogica
import cl.arzobispado.redpatrimonial.rutas.repository.*
import cl.arzobispado.redpatrimonial.rutas.service.CalculadoraRuta
import cl.arzobispado.redpatrimonial.rutas.service.GuiaService
import cl.arzobispado.redpatrimonial.rutas.service.PuntoRuta
import cl.arzobispado.redpatrimonial.rutas.service.SalidaService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional

/** Pruebas del cálculo de rutas y de las reglas de salidas pedagógicas. */
class RutasTest {

    @Test
    fun `distancia entre el mismo punto es cero`() {
        assertEquals(0.0, CalculadoraRuta.distanciaKm(-33.4378, -70.6505, -33.4378, -70.6505), 0.0001)
    }

    @Test
    fun `ruta del centro es corta y de dificultad baja`() {
        val r = CalculadoraRuta.calcular(listOf(
            PuntoRuta(-33.4378, -70.6505, 30),
            PuntoRuta(-33.4366, -70.6483, 25),
            PuntoRuta(-33.4446, -70.6466, 30)
        ))
        assertTrue(r.distanciaKm in 0.5..3.0, "distancia ${r.distanciaKm}")
        assertEquals("Baja", r.dificultad)
        assertEquals(85 + r.minutosTraslado, r.minutosTotal)
    }

    @Test
    fun `ruta centro a Maipu requiere transporte`() {
        val r = CalculadoraRuta.calcular(listOf(PuntoRuta(-33.4378, -70.6505, 30), PuntoRuta(-33.5097, -70.7660, 40)))
        assertTrue(r.distanciaKm > 6.0)
    }

    private val salidaRepo = mock(SalidaRepository::class.java)
    private val asignacionRepo = mock(AsignacionRepository::class.java)
    private val service = SalidaService(
        salidaRepo, mock(RutaRepository::class.java), mock(ObjetivoRepository::class.java), asignacionRepo,
        mock(EvaluacionRepository::class.java), mock(GuiaService::class.java), mock(GuiaRepository::class.java)
    )

    @Test
    fun `no se puede finalizar un recorrido que el guia no acepto`() {
        val salida = SalidaPedagogica(idSalida = 1)
        `when`(salidaRepo.findById(1L)).thenReturn(Optional.of(salida))
        `when`(asignacionRepo.findBySalidaIdSalida(1L)).thenReturn(AsignacionGuia(salida = salida, estadoConfirmacion = "PENDIENTE"))
        assertThrows<ReglaNegocioException> { service.finalizar(1, CambioEstadoRequest("FINALIZADA")) }
    }

    @Test
    fun `promedio del guia se actualiza correctamente`() {
        assertEquals(4.5, service.nuevoPromedio(5.0, 1, 4))
        assertEquals(5.0, service.nuevoPromedio(5.0, 0, 5))
    }
}
