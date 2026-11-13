package com.arzobispado.redpatrimonial.ui.components

import android.graphics.Bitmap
import android.graphics.Color as ColorAndroid
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Muestra un código QR real y escaneable con el texto indicado (por ejemplo "PAS-2026-48213").
 * Reemplaza al QrSimulado, que solo dibujaba un patrón parecido.
 */
@Composable
fun QrImagen(contenido: String, modifier: Modifier = Modifier) {
    // remember(contenido): el QR se genera una sola vez por código, no en cada recomposición.
    val imagen = remember(contenido) { generarQr(contenido, 512).asImageBitmap() }
    Image(
        bitmap = imagen,
        contentDescription = "Código QR $contenido",
        // Fondo blanco: los lectores de QR necesitan contraste (negro sobre blanco).
        modifier = modifier.background(Color.White)
    )
}

/**
 * Genera el QR como mapa de bits de tamaño tamano x tamano.
 * ZXing entrega una "matriz" de true/false (módulo oscuro o claro).
 * Se convierte a pixeles: negro para true, blanco para false.
 */
fun generarQr(texto: String, tamano: Int): Bitmap {
    val matriz = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tamano, tamano)
    // i es el índice del pixel: columna = i % tamano, fila = i / tamano
    val pixeles = IntArray(tamano * tamano) { i ->
        if (matriz.get(i % tamano, i / tamano)) ColorAndroid.BLACK else ColorAndroid.WHITE
    }
    return Bitmap.createBitmap(pixeles, tamano, tamano, Bitmap.Config.ARGB_8888)
}
