package com.arzobispado.redpatrimonial.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Utilidades para mostrar fechas y textos en español. */
object Formatos {
    private val locale: Locale = Locale.forLanguageTag("es-CL")
    private val diaMes = DateTimeFormatter.ofPattern("EEE d MMM", locale)
    private val diaMesLargo = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)
    private val hora = DateTimeFormatter.ofPattern("HH:mm", locale)

    /** "2026-10-24T19:00" -> "sáb 24 oct · 19:00" */
    fun fechaHora(iso: String): String = try {
        val f = LocalDateTime.parse(iso)
        "${f.format(diaMes)} · ${f.format(hora)}"
    } catch (e: Exception) { iso }

    /** "2026-10-24" -> "sábado 24 de octubre" */
    fun fechaLarga(iso: String): String = try {
        LocalDate.parse(iso.take(10)).format(diaMesLargo)
    } catch (e: Exception) { iso }

    /** "2026-10-24" -> "sáb 24 oct" */
    fun fechaCorta(iso: String): String = try {
        LocalDate.parse(iso.take(10)).format(diaMes)
    } catch (e: Exception) { iso }

    fun soloHora(iso: String): String = try { LocalDateTime.parse(iso).format(hora) } catch (e: Exception) { iso }

    fun hace(minutos: Long): String = when {
        minutos < 1 -> "Recién"
        minutos < 60 -> "Hace $minutos min"
        minutos < 60 * 24 -> "Hace ${minutos / 60} h"
        else -> "Hace ${minutos / (60 * 24)} d"
    }

    fun duracion(minutos: Int): String =
        if (minutos < 60) "$minutos min" else "${minutos / 60} h ${minutos % 60} min"

    fun dia(diaSemana: Int): String =
        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo").getOrElse(diaSemana - 1) { "?" }

    fun tipoNodo(tipo: String) = when (tipo) {
        "TEMPLO" -> "Templo"
        "MUSEO" -> "Museo"
        "COLEGIO" -> "Colegio"
        "ARCHIVO" -> "Archivo"
        else -> tipo
    }

    fun categoriaEvento(c: String) = when (c) {
        "MISA" -> "Misa"
        "CONCIERTO" -> "Concierto"
        "EXPOSICION" -> "Exposición"
        "VISITA_GUIADA" -> "Visita guiada"
        "TALLER" -> "Taller"
        else -> c
    }

    /** Valida fecha AAAA-MM-DD. */
    fun esFechaValida(texto: String): Boolean = try { LocalDate.parse(texto); true } catch (e: Exception) { false }

    /** Valida hora HH:MM. */
    fun esHoraValida(texto: String): Boolean = Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(texto)
}
