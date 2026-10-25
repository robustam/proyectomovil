package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.navigation.irAlInicio
import com.arzobispado.redpatrimonial.ui.components.Paleta
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** Pantalla de inicio de sesión. Conectada a AuthViewModel (ms-usuarios). */
@Composable
fun LoginScreen(navController: NavHostController, authVM: AuthViewModel) {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    val cargando by authVM.cargando.collectAsState()
    val mensaje by authVM.mensaje.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Paleta.Marino, Paleta.AzulOscuro)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(Icons.Default.AccountBalance, contentDescription = "Logo Red Patrimonial", modifier = Modifier.size(64.dp), tint = Paleta.Celeste)
            Spacer(Modifier.height(12.dp))
            Text("Red Patrimonial", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Arzobispado de Santiago", fontSize = 14.sp, color = Paleta.Celeste)
            Spacer(Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¡Bienvenido!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
                    Text("Inicia sesión para gestionar tus rutas y pases", fontSize = 12.sp, color = Paleta.TextoSuave, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { claveVisible = !claveVisible }) {
                                Icon(
                                    if (claveVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar u ocultar contraseña"
                                )
                            }
                        },
                        visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextButton(
                        onClick = { navController.navigate(AppScreens.RecuperarClave.route) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("¿Olvidaste tu contraseña?", fontSize = 12.sp, color = Paleta.Azul)
                    }

                    if (mensaje != null) {
                        Text(mensaje ?: "", color = Paleta.Rojo, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            authVM.login(correo, clave) { usuario -> navController.irAlInicio(usuario.rol) }
                        },
                        enabled = !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("o continúa como", fontSize = 12.sp, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            authVM.entrarComoInvitado()
                            navController.irAlInicio(Roles.TURISTA)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Paleta.TextoMedio)
                        Spacer(Modifier.width(6.dp))
                        Text("Explorar como invitado", color = Paleta.TextoMedio)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        authVM.limpiarMensaje()
                        navController.navigate(AppScreens.Register.route)
                    }) {
                        Text("¿No tienes cuenta? Regístrate aquí", fontSize = 13.sp, color = Paleta.Azul, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            // Ayuda para la demostración: cuentas de prueba ficticias
            Surface(color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Cuentas de prueba (clave: demo123)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("turista@demo.cl · profesor@demo.cl · guia@demo.cl\nencargado@demo.cl · admin@demo.cl",
                        color = Paleta.Celeste, fontSize = 11.sp)
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Login", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    val vm = rememberViewModelsPrueba()
    VistaPrevia { LoginScreen(rememberNavController(), vm.auth) }
}
