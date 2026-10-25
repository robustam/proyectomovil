package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.Aviso
import com.arzobispado.redpatrimonial.data.model.CategoriasAviso
import com.arzobispado.redpatrimonial.data.model.Roles
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.AvisoViewModel
import com.arzobispado.redpatrimonial.viewmodel.NodoViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba

/** CU14: avisos de la red en vivo (cierres, aperturas especiales, celebraciones…). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityAlertsScreen(
    navController: NavHostController,
    authVM: AuthViewModel,
    avisoVM: AvisoViewModel,
    nodoVM: NodoViewModel
) {
    val usuario by authVM.usuario.collectAsState()
    val avisos by avisoVM.avisosFiltrados.collectAsState()
    val categoria by avisoVM.categoria.collectAsState()
    var avisoAReportar by remember { mutableStateOf<Aviso?>(null) }
    val esEncargado = usuario?.rol == Roles.ENCARGADO
    LaunchedEffect(Unit) { avisoVM.cargar() }

    Pantalla(
        titulo = "Avisos de la red",
        subtitulo = "En vivo",
        onVolver = if (navController.previousBackStackEntry != null) ({ navController.popBackStack() }) else null,
        viewModels = listOf(avisoVM),
        acciones = {
            IconButton(onClick = { avisoVM.cargar() }) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White) }
        },
        fab = {
            if (esEncargado) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(AppScreens.PublicarAviso.route) },
                    containerColor = Paleta.Rojo,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    text = { Text("Publicar aviso", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FilterChip(selected = categoria == null, onClick = { avisoVM.filtrar(null) }, label = { Text("Todos") }) }
            items(CategoriasAviso.TODAS) { c ->
                FilterChip(
                    selected = categoria == c,
                    onClick = { avisoVM.filtrar(c) },
                    label = { Text("${CategoriasAviso.icono(c)} ${CategoriasAviso.nombreVisible(c)}") }
                )
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(avisos, key = { it.idAviso }) { aviso ->
                val esMio = esEncargado && aviso.idNodo == usuario?.idNodoAsignado
                TarjetaAviso(
                    aviso = aviso,
                    onTemplo = { navController.navigate(AppScreens.Detail.createRoute(aviso.idNodo)) },
                    accion = {
                        when {
                            esMio -> TextButton(onClick = { avisoVM.retirar(aviso) }) { Text("Retirar", color = Paleta.Rojo) }
                            usuario != null -> TextButton(onClick = { avisoAReportar = aviso }) {
                                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp), tint = Paleta.TextoSuave)
                                Spacer(Modifier.width(4.dp))
                                Text("Reportar", color = Paleta.TextoSuave, fontSize = 12.sp)
                            }
                        }
                    }
                )
            }
            if (avisos.isEmpty()) item { MensajeVacio("No hay avisos vigentes en esta categoría", Icons.Default.NotificationsNone) }
        }
    }

    avisoAReportar?.let { aviso ->
        val motivos = listOf("Información incorrecta", "Aviso vencido", "Contenido inapropiado")
        var motivo by remember { mutableStateOf(motivos.first()) }
        AlertDialog(
            onDismissRequest = { avisoAReportar = null },
            title = { Text("Reportar aviso") },
            text = {
                Column {
                    Text(aviso.titulo, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    motivos.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = motivo == m, onClick = { motivo = m })
                            Text(m)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    usuario?.let { avisoVM.reportar(aviso, it, motivo) }
                    avisoAReportar = null
                }) { Text("Enviar reporte") }
            },
            dismissButton = { TextButton(onClick = { avisoAReportar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun TarjetaAviso(aviso: Aviso, onTemplo: () -> Unit, accion: @Composable () -> Unit = {}) {
    val (fondo, color) = when (aviso.categoria) {
        CategoriasAviso.CIERRE_TEMPORAL -> Paleta.RojoSuave to Paleta.RojoOscuro
        CategoriasAviso.APERTURA_ESPECIAL -> Paleta.VerdeSuave to Paleta.Verde
        CategoriasAviso.CELEBRACION -> Paleta.MoradoSuave to Paleta.Morado
        CategoriasAviso.CONSERVACION -> Paleta.AmbarSuave to Paleta.Ambar
        else -> Paleta.AzulSuave to Paleta.AzulTexto
    }
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Etiqueta("${CategoriasAviso.icono(aviso.categoria)} ${CategoriasAviso.nombreVisible(aviso.categoria)}", fondo, color)
            Spacer(Modifier.weight(1f))
            Text(Formatos.hace(aviso.haceMinutos), fontSize = 11.sp, color = Paleta.TextoSuave)
        }
        Spacer(Modifier.height(6.dp))
        Text(aviso.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.Texto)
        TextButton(onClick = onTemplo, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.Default.Church, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(aviso.nombreNodo, fontSize = 12.sp)
        }
        Text(aviso.mensaje, fontSize = 13.sp, color = Paleta.TextoMedio)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Vigente hasta ${Formatos.fechaHora(aviso.fechaExpiracion)}", fontSize = 11.sp, color = Paleta.TextoSuave, modifier = Modifier.weight(1f))
            accion()
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Avisos de la red", showBackground = true, showSystemUi = true)
@Composable
private fun CommunityAlertsScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA)
    VistaPrevia { CommunityAlertsScreen(rememberNavController(), vm.auth, vm.aviso, vm.nodo) }
}
