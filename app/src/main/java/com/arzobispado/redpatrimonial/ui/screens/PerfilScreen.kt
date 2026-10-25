package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.data.remote.ApiConfig
import com.arzobispado.redpatrimonial.data.repository.EstadoConexion
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.EventoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import com.arzobispado.redpatrimonial.viewmodel.RutaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** Perfil: entradas QR, rutas guardadas, favoritos y cierre de sesión. */
@Composable
fun PerfilScreen(
    navController: NavHostController,
    authVM: AuthViewModel,
    eventoVM: EventoViewModel,
    rutaVM: RutaViewModel,
    nodoVM: NodoViewModel
) {
    val usuario by authVM.usuario.collectAsState()
    val entradas by eventoVM.misInscripciones.collectAsState()
    val rutas by rutaVM.rutas.collectAsState()
    val favoritos by nodoVM.favoritos.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val enLinea by EstadoConexion.enLinea.collectAsState()

    LaunchedEffect(usuario?.idUsuario) {
        usuario?.let {
            eventoVM.cargarInscripciones(it.idUsuario)
            rutaVM.cargar(it.idUsuario)
        }
    }

    Pantalla(titulo = "Mi perfil", viewModels = listOf(eventoVM, authVM)) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val u = usuario
            if (u == null) {
                Tarjeta {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Paleta.TextoSuave, modifier = Modifier.size(56.dp))
                    Text("Estás navegando como invitado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Crea una cuenta para reservar cupos, guardar rutas y favoritos.", fontSize = 13.sp, color = Paleta.TextoSuave)
                    Spacer(Modifier.height(12.dp))
                    BotonPrincipal("Iniciar sesión", onClick = { navController.navigate(AppScreens.Login.route) })
                    TextButton(onClick = { navController.navigate(AppScreens.Register.route) }) { Text("Crear una cuenta nueva") }
                }
            } else {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .background(Paleta.AzulSuave, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(u.nombreCompleto.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Paleta.Azul)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(u.nombreCompleto, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Paleta.Texto)
                            Text(u.email, fontSize = 13.sp, color = Paleta.TextoSuave)
                            Spacer(Modifier.height(4.dp))
                            Etiqueta(Roles.nombreVisible(u.rol))
                        }
                    }
                }

                // Accesos según el rol
                when (u.rol) {
                    Roles.PROFESOR -> OpcionPerfil(Icons.Default.School, "Mis salidas pedagógicas") { navController.navigate(AppScreens.Salidas.route) }
                    Roles.GUIA -> OpcionPerfil(Icons.Default.Badge, "Panel de guía y disponibilidad") { navController.navigate(AppScreens.PanelGuia.route) }
                    Roles.ENCARGADO -> OpcionPerfil(Icons.Default.Church, "Gestionar mi templo") { navController.navigate(AppScreens.PanelEncargado.route) }
                    Roles.ADMIN -> OpcionPerfil(Icons.Default.Dashboard, "Panel de administración") { navController.navigate(AppScreens.PanelAdmin.route) }
                }

                OpcionPerfil(Icons.Default.Explore, "Mis rutas guardadas (${rutas.count { it.personalizada }})") {
                    navController.navigate(AppScreens.RoutePlanner.route)
                }

                TituloSeccion("Mis entradas y códigos QR (${entradas.count { it.estadoAsistencia == "CONFIRMADA" }})")
                if (entradas.isEmpty()) Text("Aún no reservas cupos en eventos.", fontSize = 13.sp, color = Paleta.TextoSuave)
                entradas.forEach { e ->
                    Tarjeta(Modifier.clickable {
                        eventoVM.verTicket(e)
                        navController.navigate(AppScreens.Ticket.route)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = Paleta.Azul)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(e.tituloEvento, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${Formatos.fechaHora(e.fechaEvento)} · ${e.codigoQr}", fontSize = 12.sp, color = Paleta.TextoSuave)
                            }
                            Etiqueta(e.estadoAsistencia.lowercase().replaceFirstChar { it.uppercase() },
                                if (e.estadoAsistencia == "CONFIRMADA") Paleta.VerdeSuave else Paleta.Borde,
                                if (e.estadoAsistencia == "CONFIRMADA") Paleta.Verde else Paleta.TextoMedio)
                        }
                    }
                }

                TituloSeccion("Mis favoritos (${favoritos.size})")
                if (favoritos.isEmpty()) Text("Toca el corazón en una ficha para guardarla aquí.", fontSize = 13.sp, color = Paleta.TextoSuave)
                nodos.filter { it.idNodo in favoritos }.forEach { n ->
                    TarjetaNodo(n, onClick = { navController.navigate(AppScreens.Detail.createRoute(n.idNodo)) })
                }
            }

            TituloSeccion("Configuración")
            Tarjeta {
                FilaDato(Icons.Default.Dns, "Servidor: ${ApiConfig.HOST} (puertos 8081-8085)")
                FilaDato(
                    if (enLinea) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    if (enLinea) "Conectado a los microservicios" else "Sin conexión: usando datos de prueba",
                    tinte = if (enLinea) Paleta.Verde else Paleta.Ambar
                )
            }
            if (u != null) {
                OutlinedButton(
                    onClick = {
                        authVM.logout()
                        navController.navigate(AppScreens.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Paleta.Rojo)
                    Spacer(Modifier.width(6.dp))
                    Text("Cerrar sesión", color = Paleta.Rojo)
                }
            }
        }
    }
}

@Composable
private fun OpcionPerfil(icono: ImageVector, texto: String, onClick: () -> Unit) {
    Surface(color = Color.White, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = Paleta.Azul)
            Spacer(Modifier.width(12.dp))
            Text(texto, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Paleta.TextoSuave)
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Perfil", showBackground = true, showSystemUi = true)
@Composable
private fun PerfilScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        usuario?.let { evento.cargarInscripciones(it.idUsuario); ruta.cargar(it.idUsuario) }
        nodo.alternarFavorito(1)
    }
    VistaPrevia { PerfilScreen(rememberNavController(), vm.auth, vm.evento, vm.ruta, vm.nodo) }
}
