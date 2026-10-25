package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import com.arzobispado.redpatrimonial.data.model.Ruta
import com.arzobispado.redpatrimonial.navigation.AppScreens
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.util.Formatos
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.RutaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** Rutas recomendadas por la red y rutas personalizadas del usuario (CU06). */
@Composable
fun RoutePlannerScreen(navController: NavHostController, authVM: AuthViewModel, rutaVM: RutaViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val rutas by rutaVM.rutas.collectAsState()
    LaunchedEffect(usuario?.idUsuario) { rutaVM.cargar(usuario?.idUsuario) }

    val recomendadas = rutas.filter { !it.personalizada }
    val mias = rutas.filter { it.personalizada }

    Pantalla(
        titulo = "Rutas patrimoniales",
        onVolver = if (navController.previousBackStackEntry != null) ({ navController.popBackStack() }) else null,
        viewModels = listOf(rutaVM),
        fab = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(AppScreens.CrearRuta.route) },
                containerColor = Paleta.Azul,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Crear mi ruta", fontWeight = FontWeight.Bold) }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TituloSeccion("Rutas recomendadas") }
            items(recomendadas, key = { it.idRuta }) { ruta ->
                ItemRuta(ruta, onVer = { navController.navigate(AppScreens.RutaDetalle.createRoute(ruta.idRuta)) })
            }
            item { TituloSeccion("Mis rutas personalizadas", Modifier.padding(top = 8.dp)) }
            if (usuario == null) {
                item { MensajeVacio("Inicia sesión para guardar tus propias rutas", Icons.Default.Lock) }
            } else if (mias.isEmpty()) {
                item { MensajeVacio("Aún no creas rutas. Toca \"Crear mi ruta\" y elige los templos que quieres visitar.", Icons.Default.AddLocationAlt) }
            }
            items(mias, key = { it.idRuta }) { ruta ->
                ItemRuta(
                    ruta,
                    onVer = { navController.navigate(AppScreens.RutaDetalle.createRoute(ruta.idRuta)) },
                    onEliminar = { rutaVM.eliminarRuta(ruta, usuario?.idUsuario) }
                )
            }
        }
    }
}

@Composable
fun ItemRuta(ruta: Ruta, onVer: () -> Unit, onEliminar: (() -> Unit)? = null) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Etiqueta(ruta.enfoque)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Timer, contentDescription = null, tint = Paleta.TextoSuave, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(Formatos.duracion(ruta.tiempoEstimadoMin), fontSize = 12.sp, color = Paleta.TextoSuave)
            if (onEliminar != null) {
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Paleta.TextoSuave, modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(ruta.nombreRuta, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Paleta.Texto)
        if (ruta.descripcion.isNotBlank()) Text(ruta.descripcion, fontSize = 13.sp, color = Paleta.TextoMedio)
        Spacer(Modifier.height(4.dp))
        Text(ruta.paradas.joinToString(" → ") { it.nombreNodo }, fontSize = 12.sp, color = Paleta.TextoSuave, maxLines = 2)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📏 ${ruta.distanciaKm} km · ${ruta.paradas.size} paradas · ${ruta.nivelDificultad}",
                fontSize = 12.sp, color = Paleta.TextoMedio, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onVer, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ver recorrido", fontSize = 12.sp)
            }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Rutas", showBackground = true, showSystemUi = true)
@Composable
private fun RoutePlannerScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.TURISTA) {
        ruta.cargar(1)
    }
    VistaPrevia { RoutePlannerScreen(rememberNavController(), vm.auth, vm.ruta) }
}
