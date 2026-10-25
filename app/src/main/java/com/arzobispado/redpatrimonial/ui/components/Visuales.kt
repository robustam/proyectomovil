package com.arzobispado.redpatrimonial.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Convierte latitud/longitud en coordenadas de pantalla para un área de dibujo.
 * Corrige la longitud con cos(latitud) para que el mapa no se vea estirado.
 */
private class Proyeccion(puntos: List<Pair<Double, Double>>, private val ancho: Float, private val alto: Float) {
    private val minLat = puntos.minOf { it.first }
    private val maxLat = puntos.maxOf { it.first }
    private val minLng = puntos.minOf { it.second }
    private val maxLng = puntos.maxOf { it.second }
    private val factorLng = cos(Math.toRadians((minLat + maxLat) / 2))
    private val margen = 36f
    private val escala: Double
    private val desplX: Float
    private val desplY: Float

    init {
        val anchoGeo = ((maxLng - minLng) * factorLng).coerceAtLeast(0.005)
        val altoGeo = (maxLat - minLat).coerceAtLeast(0.005)
        escala = minOf((ancho - 2 * margen) / anchoGeo, (alto - 2 * margen) / altoGeo)
        desplX = ((ancho - anchoGeo * escala) / 2).toFloat()
        desplY = ((alto - altoGeo * escala) / 2).toFloat()
    }

    fun aPantalla(lat: Double, lng: Double) = Offset(
        x = (desplX + (lng - minLng) * factorLng * escala).toFloat(),
        y = (desplY + (maxLat - lat) * escala).toFloat()
    )
}

/** Trazado aproximado del río Mapocho, solo como referencia visual. */
private val RIO_MAPOCHO = listOf(
    -33.395 to -70.52, -33.412 to -70.58, -33.428 to -70.63, -33.432 to -70.66, -33.430 to -70.70, -33.445 to -70.78
)

/**
 * Mapa interactivo esquemático de la red (CU01).
 * Dibuja los templos según sus coordenadas de prueba. No usa GPS ni Google Maps:
 * cuando en clases vean recursos del teléfono se puede reemplazar por un mapa real.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun MapaEsquematico(
    nodos: List<NodoPatrimonial>,
    seleccionado: NodoPatrimonial?,
    onSeleccionar: (NodoPatrimonial) -> Unit,
    modifier: Modifier = Modifier,
    ruta: List<Pair<Double, Double>> = emptyList()
) {
    val medidor = rememberTextMeasurer()
    val puntos = nodos.map { it.latitud to it.longitud } + ruta
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE9EFE6))
    ) {
        if (puntos.isEmpty()) return@Box
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(nodos) {
                    detectTapGestures { toque ->
                        val p = Proyeccion(puntos, size.width.toFloat(), size.height.toFloat())
                        val cercano = nodos.minByOrNull {
                            val o = p.aPantalla(it.latitud, it.longitud)
                            hypot(o.x - toque.x, o.y - toque.y)
                        }
                        if (cercano != null) {
                            val o = p.aPantalla(cercano.latitud, cercano.longitud)
                            if (hypot(o.x - toque.x, o.y - toque.y) < 60f) onSeleccionar(cercano)
                        }
                    }
                }
        ) {
            val p = Proyeccion(puntos, size.width, size.height)

            // Cuadrícula de "calles"
            val paso = 48.dp.toPx()
            var x = 0f
            while (x < size.width) { drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 2f); x += paso }
            var y = 0f
            while (y < size.height) { drawLine(Color.White, Offset(0f, y), Offset(size.width, y), 2f); y += paso }

            // Río Mapocho
            val rio = Path()
            RIO_MAPOCHO.forEachIndexed { i, (lat, lng) ->
                val o = p.aPantalla(lat, lng)
                if (i == 0) rio.moveTo(o.x, o.y) else rio.lineTo(o.x, o.y)
            }
            drawPath(rio, Color(0xFF93C5FD), style = Stroke(width = 10f, cap = StrokeCap.Round))

            // Ruta (si se está mostrando una)
            if (ruta.size > 1) {
                val camino = Path()
                ruta.forEachIndexed { i, (lat, lng) ->
                    val o = p.aPantalla(lat, lng)
                    if (i == 0) camino.moveTo(o.x, o.y) else camino.lineTo(o.x, o.y)
                }
                drawPath(camino, Paleta.Azul, style = Stroke(width = 6f, cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))))
            }

            // Marcadores de templos con el color de su estado
            nodos.forEach { n ->
                val o = p.aPantalla(n.latitud, n.longitud)
                val elegido = n.idNodo == seleccionado?.idNodo
                val radio = if (elegido) 16f else 11f
                drawCircle(Color.White, radius = radio + 4f, center = o)
                drawCircle(colorEstado(n.estadoActual), radius = radio, center = o)
                if (elegido) {
                    drawCircle(Paleta.Marino, radius = radio + 9f, center = o, style = Stroke(width = 3f))
                    val texto = medidor.measure(n.nombreOficial.take(28), TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Paleta.Marino))
                    val tx = (o.x - texto.size.width / 2).coerceIn(4f, size.width - texto.size.width - 4f)
                    val ty = (o.y - radio - 14f - texto.size.height).coerceAtLeast(4f)
                    drawRoundRect(Color.White, topLeft = Offset(tx - 6f, ty - 3f),
                        size = Size(texto.size.width + 12f, texto.size.height + 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f))
                    drawText(texto, topLeft = Offset(tx, ty))
                }
            }
        }
        Text(
            "Mapa esquemático · coordenadas de prueba",
            fontSize = 10.sp,
            color = Paleta.TextoSuave,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Código QR SIMULADO (patrón visual generado a partir del código del ticket).
 * Para un QR real escaneable se puede agregar la librería ZXing más adelante.
 */
@Composable
fun QrSimulado(codigo: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color.White)) {
        val n = 25
        val celda = size.minDimension / n
        val azar = Random(codigo.hashCode())
        fun esBuscador(f: Int, c: Int): Boolean {
            fun en(f0: Int, c0: Int) = f in f0 until f0 + 7 && c in c0 until c0 + 7
            return en(0, 0) || en(0, n - 7) || en(n - 7, 0)
        }
        fun moduloBuscador(f: Int, c: Int): Boolean {
            val ff = if (f >= n - 7) f - (n - 7) else f
            val cc = if (c >= n - 7) c - (n - 7) else c
            val borde = ff == 0 || ff == 6 || cc == 0 || cc == 6
            val centro = ff in 2..4 && cc in 2..4
            return borde || centro
        }
        for (f in 0 until n) for (c in 0 until n) {
            val negro = if (esBuscador(f, c)) moduloBuscador(f, c) else azar.nextBoolean()
            if (negro) drawRect(Color.Black, topLeft = Offset(c * celda, f * celda), size = Size(celda, celda))
        }
    }
}

/** Imagen de galería simulada (los recursos multimedia son URLs de prueba). */
@Composable
fun RecursoSimulado(titulo: String, tipo: String, modifier: Modifier = Modifier) {
    val icono = when (tipo) {
        "AUDIO" -> Icons.Default.Headphones
        "VIDEO" -> Icons.Default.Videocam
        else -> Icons.Default.Image
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Paleta.AzulOscuro, Paleta.Azul))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
            Icon(icono, contentDescription = null, tint = Paleta.Celeste, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(4.dp))
            Text(titulo, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        }
    }
}
