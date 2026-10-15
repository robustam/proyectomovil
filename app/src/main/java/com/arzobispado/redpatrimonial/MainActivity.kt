package com.arzobispado.redpatrimonial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.arzobispado.redpatrimonial.navigation.AppNavigation
import com.arzobispado.redpatrimonial.ui.theme.RedPatrimonialAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el diseño a pantalla completa
        enableEdgeToEdge()

        // setContent carga la interfaz de Jetpack Compose
        setContent {
            RedPatrimonialAppTheme {
                // Surface aplica el fondo según el tema Material 3
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Carga el sistema de navegación global
                    AppNavigation()
                }
            }
        }
    }
}