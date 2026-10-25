package com.arzobispado.redpatrimonial.util

import com.arzobispado.redpatrimonial.data.model.Parada
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class ResultadoRuta(val distanciaKm: Double, val minutosTraslado: Int, val minutosTotal: Int, val dificultad: String)

/**
 * Misma fórmula que usa ms-rutas: distancia Haversine entre paradas consecutivas,
 * factor 1,3 por las calles y 4,5 km/h caminando. Sirve para mostrar el tiempo
 * estimado mientras el usuario arma su ruta. NO usa el GPS del teléfono.
 */
object CalculadoraRuta {
    private const val RADIO_TIERRA_KM = 6371.0
    private const val FACTOR_CALLES = 1.3
    private const val VELOCIDAD_KMH = 4.5

    fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
    }

    fun calcular(paradas: List<Parada>): ResultadoRuta {
        val distancia = paradas.zipWithNext { a, b ->
            distanciaKm(a.latitud, a.longitud, b.latitud, b.longitud) * FACTOR_CALLES
        }.sum()
        val traslado = (distancia / VELOCIDAD_KMH * 60).roundToInt()
        val dificultad = when {
            distancia <= 3.0 -> "Baja"
            distancia <= 6.0 -> "Media"
            else -> "Alta (requiere transporte)"
        }
        return ResultadoRuta((distancia * 10).roundToInt() / 10.0, traslado, traslado + paradas.sumOf { it.tiempoSugeridoMin }, dificultad)
    }
}
