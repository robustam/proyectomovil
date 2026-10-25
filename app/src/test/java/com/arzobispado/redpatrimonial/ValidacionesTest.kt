package com.arzobispado.redpatrimonial

import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import com.arzobispado.redpatrimonial.data.model.Parada
import com.arzobispado.redpatrimonial.util.CalculadoraRuta
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.arzobispado.redpatrimonial.viewmodel.FiltrosNodo
import com.arzobispado.redpatrimonial.viewmodel.FormularioSalida
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import com.arzobispado.redpatrimonial.viewmodel.SalidaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas unitarias de las reglas de validación y cálculo de la app. */
class ValidacionesTest {

    @Test
    fun login_rechaza_correo_invalido() {
        assertNotNull(AuthViewModel.validarLogin("correo-sin-arroba", "123456"))
        assertNull(AuthViewModel.validarLogin("turista@demo.cl", "demo123"))
    }

    @Test
    fun registro_exige_claves_iguales_y_largo_minimo() {
        assertNotNull(AuthViewModel.validarRegistro("Ana Silva", "ana@demo.cl", "123", "123"))
        assertNotNull(AuthViewModel.validarRegistro("Ana Silva", "ana@demo.cl", "123456", "654321"))
        assertNull(AuthViewModel.validarRegistro("Ana Silva", "ana@demo.cl", "123456", "123456"))
    }

    @Test
    fun evento_valida_horas() {
        assertNotNull(EventoViewModel.validarEvento("Concierto", "2030-01-10", "20:00", "19:00", "10"))
        assertNull(EventoViewModel.validarEvento("Concierto", "2030-01-10", "19:00", "20:30", "10"))
    }

    @Test
    fun salida_paso1_valida_cantidad_de_estudiantes() {
        val manana = LocalDate.now().plusDays(1).toString()
        val f = FormularioSalida(colegio = "Colegio", curso = "8°A", cantidadAlumnos = "0", fecha = manana, hora = "10:00")
        assertNotNull(SalidaViewModel.validarPaso(1, f))
        assertNull(SalidaViewModel.validarPaso(1, f.copy(cantidadAlumnos = "35")))
    }

    @Test
    fun salida_paso2_exige_ruta_y_objetivos() {
        assertNotNull(SalidaViewModel.validarPaso(2, FormularioSalida()))
        assertNull(SalidaViewModel.validarPaso(2, FormularioSalida(idRuta = 1, objetivos = setOf(1L))))
    }

    private fun nodo(id: Long, nombre: String, comuna: String, estado: String) =
        NodoPatrimonial(id, nombre, "", comuna, "TEMPLO", latitud = 0.0, longitud = 0.0, estadoActual = estado)

    @Test
    fun filtros_por_comuna_y_abiertos() {
        val lista = listOf(
            nodo(1, "Catedral", "Santiago", EstadosApertura.ABIERTO),
            nodo(2, "Templo Votivo", "Maipú", EstadosApertura.ABIERTO),
            nodo(3, "Basílica", "Santiago", EstadosApertura.CERRADO)
        )
        assertEquals(2, NodoViewModel.aplicarFiltros(lista, FiltrosNodo(comuna = "Santiago")).size)
        assertEquals(1, NodoViewModel.aplicarFiltros(lista, FiltrosNodo(comuna = "Santiago", soloAbiertos = true)).size)
        assertEquals(1, NodoViewModel.aplicarFiltros(lista, FiltrosNodo(texto = "votivo")).size)
    }

    @Test
    fun calculo_de_ruta_suma_visitas_y_traslados() {
        val r = CalculadoraRuta.calcular(listOf(
            Parada(idNodo = 1, nombreNodo = "Catedral", latitud = -33.4378, longitud = -70.6505, tiempoSugeridoMin = 30),
            Parada(idNodo = 4, nombreNodo = "Santo Domingo", latitud = -33.4366, longitud = -70.6483, tiempoSugeridoMin = 25)
        ))
        assertEquals(55 + r.minutosTraslado, r.minutosTotal)
        assertEquals("Baja", r.dificultad)
    }

    @Test
    fun formatos_de_tiempo() {
        assertEquals("Hace 12 min", Formatos.hace(12))
        assertEquals("1 h 45 min", Formatos.duracion(105))
        assertTrue(Formatos.esHoraValida("09:30"))
        assertTrue(!Formatos.esHoraValida("25:00"))
    }
}
