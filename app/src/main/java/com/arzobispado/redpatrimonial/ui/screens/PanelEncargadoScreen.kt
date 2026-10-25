package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.arzobispado.redpatrimonial.data.model.EstadosApertura
import com.arzobispado.redpatrimonial.data.model.EstadosSalida
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.AvisoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import com.arzobispado.redpatrimonial.viewmodel.SalidaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/**
 * Panel del encargado de templo: el "nodo autónomo" del anexo.
 * Cambia el estado en tiempo real y accede a horarios, eventos, avisos, solicitudes y ficha.
 */
@Composable
fun PanelEncargadoScreen(
    navController: NavHostController,
    authVM: AuthViewModel,
    nodoVM: NodoViewModel,
    salidaVM: SalidaViewModel,
    avisoVM: AvisoViewModel
) {
    val usuario by authVM.usuario.collectAsState()
    val nodos by nodoVM.nodos.collectAsState()
    val solicitudes by salidaVM.salidas.collectAsState()
    val avisos by avisoVM.avisos.collectAsState()
    val idNodo = usuario?.idNodoAsignado
    val nodo = nodos.firstOrNull { it.idNodo == idNodo }
    LaunchedEffect(idNodo) { idNodo?.let { salidaVM.cargarDeNodo(it) } }

    val pendientes = solicitudes.count { it.estadoSalida == EstadosSalida.PENDIENTE }
    val misAvisos = avisos.filter { it.idNodo == idNodo }

    Pantalla(titulo = "Mi templo", subtitulo = "Encargado: ${usuario?.nombreCompleto ?: ""}", viewModels = listOf(nodoVM, salidaVM, avisoVM, authVM)) {
        if (nodo == null) {
            MensajeVacio(
                if (idNodo == null) "No tienes un templo asignado. Pide al administrador de la red que te asigne uno."
                else "Cargando información del templo…",
                Icons.Default.Church
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { TarjetaEstado(nodo) { nodoVM.cambiarEstado(nodo.idNodo, it) } }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Indicador("$pendientes", "Solicitudes pendientes", Modifier.weight(1f))
                        Indicador("${misAvisos.size}", "Avisos activos", Modifier.weight(1f))
                        Indicador("${nodo.horariosExcepcionales.size}", "Horarios especiales", Modifier.weight(1f))
                    }
                }
                item { TituloSeccion("Gestión del nodo") }
                item { Accion(Icons.Default.Schedule, "Horarios excepcionales", "Aperturas especiales, cierres o celebraciones") { navController.navigate(AppScreens.HorarioExcepcional.route) } }
                item { Accion(Icons.Default.Event, "Publicar evento cultural o litúrgico", "Conciertos, misas, exposiciones, talleres") { navController.navigate(AppScreens.PublicarEvento.route) } }
                item { Accion(Icons.Default.Campaign, "Publicar aviso en la red", "Se ve de inmediato en el mapa y en Avisos") { navController.navigate(AppScreens.PublicarAviso.route) } }
                item { Accion(Icons.Default.Groups, "Solicitudes de visitas guiadas", "$pendientes pendiente(s) de confirmar") { navController.navigate(AppScreens.Solicitudes.route) } }
                item { Accion(Icons.Default.Edit, "Editar ficha y galería", "Reseña, datos y recursos multimedia") { navController.navigate(AppScreens.EditarFicha.route) } }
                item { Accion(Icons.Default.Visibility, "Ver ficha pública", "Así ven tu templo los visitantes") { navController.navigate(AppScreens.Detail.createRoute(nodo.idNodo)) } }

                if (misAvisos.isNotEmpty()) {
                    item { TituloSeccion("Mis avisos activos") }
                    items(misAvisos, key = { it.idAviso }) { a ->
                        TarjetaAviso(a, onTemplo = {}) {
                            TextButton(onClick = { avisoVM.retirar(a) }) { Text("Retirar", color = Paleta.Rojo) }
                        }
                    }
                }
            }
        }
    }
}

/** Cambio de estado en tiempo real (CU04/CU11). */
@Composable
private fun TarjetaEstado(nodo: NodoPatrimonial, onCambiar: (String) -> Unit) {
    Tarjeta {
        Text(nodo.nombreOficial, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Paleta.Texto)
        Text("${nodo.direccion}, ${nodo.comuna}", fontSize = 12.sp, color = Paleta.TextoSuave)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Estado ahora: ", fontSize = 13.sp, color = Paleta.TextoMedio)
            EtiquetaEstado(nodo.estadoActual)
        }
        Spacer(Modifier.height(10.dp))
        Text("Cambiar estado en tiempo real", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Paleta.TextoSuave)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EstadosApertura.TODOS.forEach { e ->
                val activo = nodo.estadoOperativo == e
                Button(
                    onClick = { onCambiar(e) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activo) colorEstado(e) else Color.White,
                        contentColor = if (activo) Color.White else colorEstado(e)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorEstado(e)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) { Text(EstadosApertura.nombreVisible(e), fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun Accion(icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    Surface(color = Color.White, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = Paleta.Azul)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(detalle, fontSize = 12.sp, color = Paleta.TextoSuave)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Paleta.TextoSuave)
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Panel del encargado", showBackground = true, showSystemUi = true)
@Composable
private fun PanelEncargadoScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO) {
        usuario?.idNodoAsignado?.let { salida.cargarDeNodo(it) }
    }
    VistaPrevia { PanelEncargadoScreen(rememberNavController(), vm.auth, vm.nodo, vm.salida, vm.aviso) }
}
