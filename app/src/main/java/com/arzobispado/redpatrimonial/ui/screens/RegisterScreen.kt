package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.navigation.irAlInicio
import com.arzobispado.redpatrimonial.ui.components.Paleta
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

private data class OpcionPerfil(val rol: String, val titulo: String, val detalle: String, val icono: ImageVector)

/** Registro con selección de perfil (Visitante, Profesor o Guía), como en la maqueta. */
@Composable
fun RegisterScreen(navController: NavHostController, authVM: AuthViewModel) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var rol by remember { mutableStateOf(Roles.TURISTA) }
    var aceptaTerminos by remember { mutableStateOf(false) }
    val cargando by authVM.cargando.collectAsState()
    val mensaje by authVM.mensaje.collectAsState()

    val perfiles = listOf(
        OpcionPerfil(Roles.TURISTA, "Visitante / Turista", "Explora templos, audioguías y rutas", Icons.Default.Map),
        OpcionPerfil(Roles.PROFESOR, "Profesor / Encargado escolar", "Organiza salidas pedagógicas con cursos", Icons.Default.School),
        OpcionPerfil(Roles.GUIA, "Guía voluntario", "Acompaña grupos y realiza recorridos", Icons.Default.Badge)
    )

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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    authVM.limpiarMensaje()
                    navController.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }
            Text("Crear cuenta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Únete a la Red Patrimonial", fontSize = 14.sp, color = Paleta.Celeste)
            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("DATOS PERSONALES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(correo, { correo = it }, label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, null) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(clave, { clave = it }, label = { Text("Contraseña (mín. 6 caracteres)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { claveVisible = !claveVisible }) {
                                Icon(if (claveVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(confirmar, { confirmar = it }, label = { Text("Confirmar contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                    Spacer(Modifier.height(18.dp))
                    Text("SELECCIONA TU PERFIL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(8.dp))
                    perfiles.forEach { p ->
                        val elegido = rol == p.rol
                        OutlinedCard(
                            onClick = { rol = p.rol },
                            border = BorderStroke(if (elegido) 2.dp else 1.dp, if (elegido) Paleta.Azul else Paleta.Borde),
                            colors = CardDefaults.outlinedCardColors(containerColor = if (elegido) Paleta.AzulMuySuave else Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(p.icono, contentDescription = null, tint = Paleta.Azul)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Paleta.Texto)
                                    Text(p.detalle, fontSize = 12.sp, color = Paleta.TextoSuave)
                                }
                                RadioButton(selected = elegido, onClick = { rol = p.rol })
                            }
                        }
                    }
                    Text("Los encargados de templo son habilitados por el administrador de la red.",
                        fontSize = 11.sp, color = Paleta.TextoSuave, modifier = Modifier.padding(top = 4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = aceptaTerminos, onCheckedChange = { aceptaTerminos = it })
                        Text("Acepto los Términos y Políticas de Privacidad", fontSize = 12.sp, color = Paleta.TextoMedio)
                    }

                    if (mensaje != null) {
                        Text(mensaje ?: "", color = Paleta.Rojo, fontSize = 13.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            authVM.registrar(nombre, correo, clave, confirmar, rol) { u -> navController.irAlInicio(u.rol) }
                        },
                        enabled = aceptaTerminos && !cargando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Paleta.Azul)
                    ) {
                        Text("Completar registro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("¿Ya tienes cuenta? Inicia sesión", color = Paleta.Azul, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Registro", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    val vm = rememberViewModelsPrueba()
    VistaPrevia { RegisterScreen(rememberNavController(), vm.auth) }
}
