package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** Recuperar contraseña (simulado: en el MVP no se envían correos reales). */
@Composable
fun RecuperarClaveScreen(navController: NavHostController, authVM: AuthViewModel) {
    var correo by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }

    Pantalla(titulo = "Recuperar contraseña", onVolver = { navController.popBackStack() }, viewModels = listOf(authVM)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.LockReset, contentDescription = null, tint = Paleta.Azul, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                if (enviado) "Revisa tu bandeja de entrada. Si el correo está registrado, te llegarán las instrucciones."
                else "Ingresa el correo con el que te registraste y te enviaremos un enlace para crear una nueva contraseña.",
                textAlign = TextAlign.Center, color = Paleta.TextoMedio, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))
            CampoFormulario(correo, { correo = it }, "Correo electrónico", icono = Icons.Default.Email, teclado = KeyboardType.Email)
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(
                texto = if (enviado) "Volver al inicio de sesión" else "Enviar enlace",
                icono = if (enviado) null else Icons.AutoMirrored.Filled.Send,
                onClick = {
                    if (enviado) navController.popBackStack()
                    else authVM.recuperarClave(correo) { enviado = true }
                }
            )
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Recuperar contraseña", showBackground = true, showSystemUi = true)
@Composable
private fun RecuperarClaveScreenPreview() {
    val vm = rememberViewModelsPrueba()
    VistaPrevia { RecuperarClaveScreen(rememberNavController(), vm.auth) }
}
