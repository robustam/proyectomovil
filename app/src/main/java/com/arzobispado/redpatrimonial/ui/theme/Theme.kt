package com.arzobispado.redpatrimonial.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema de la app. Se usa siempre el esquema claro con los colores de la marca
 * para que todas las pantallas se vean igual en cualquier teléfono.
 */
private val EsquemaRedPatrimonial = lightColorScheme(
    primary = AzulRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = AzulOscuro,
    secondary = AzulOscuro,
    onSecondary = Color.White,
    tertiary = Dorado,
    background = FondoClaro,
    surface = Color.White,
    onBackground = Marino,
    onSurface = Marino
)

@Composable
fun RedPatrimonialAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaRedPatrimonial,
        typography = Typography,
        content = content
    )
}
