package cl.arzobispado.redpatrimonial.rutas.service

import kotlin.math.*

/** Punto de una ruta (coordenadas de prueba del anexo). */
data class PuntoRuta(val latitud: Double, val longitud: Double, val minutosVisita: Int)

data class ResultadoRuta(val distanciaKm: Double, val minutosTraslado: Int, val minutosTotal: Int, val dificultad: String)

/**
 * Cálculo estimado de distancia y tiempo de una ruta patrimonial (anexo 2.5:
 * "trazado dinámico de rutas personalizadas con el cálculo estimado de sus tiempos").
 * Usa la fórmula de Haversine entre paradas consecutivas. No usa el GPS del teléfono:
 * trabaja solo con las coordenadas guardadas de cada templo.
 */
object CalculadoraRuta {
    private const val RADIO_TIERRA_KM = 6371.0
    /** Las calles no son línea recta: se aplica un factor de corrección. */
    private const val FACTOR_CALLES = 1.3
    /** Velocidad promedio caminando con un grupo. */
    private const val VELOCIDAD_KMH = 4.5

    fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
    }

    fun calcular(puntos: List<PuntoRuta>): ResultadoRuta {
        val distancia = puntos.zipWithNext { a, b ->
            distanciaKm(a.latitud, a.longitud, b.latitud, b.longitud) * FACTOR_CALLES
        }.sum()
        val traslado = (distancia / VELOCIDAD_KMH * 60).roundToInt()
        val visitas = puntos.sumOf { it.minutosVisita }
        val dificultad = when {
            distancia <= 3.0 -> "Baja"
            distancia <= 6.0 -> "Media"
            else -> "Alta (requiere transporte)"
        }
        return ResultadoRuta((distancia * 10).roundToInt() / 10.0, traslado, traslado + visitas, dificultad)
    }
}
