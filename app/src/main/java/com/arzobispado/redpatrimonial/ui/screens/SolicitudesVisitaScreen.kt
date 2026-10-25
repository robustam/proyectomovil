package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arzobispado.redpatrimonial.data.model.EstadosSalida
import com.arzobispado.redpatrimonial.ui.components.*
import com.arzobispado.redpatrimonial.viewmodel.AuthViewModel
import com.arzobispado.redpatrimonial.viewmodel.SalidaViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.ui.preview.VistaPrevia
import com.arzobispado.redpatrimonial.ui.preview.rememberViewModelsPrueba
import com.arzobispado.redpatrimonial.data.model.Roles

/** CU10 / CU15: el encargado confirma o rechaza las visitas grupales que pasan por su templo. */
@Composable
fun SolicitudesVisitaScreen(navController: NavHostController, authVM: AuthViewModel, salidaVM: SalidaViewModel) {
    val usuario by authVM.usuario.collectAsState()
    val solicitudes by salidaVM.salidas.collectAsState()
    val idNodo = usuario?.idNodoAsignado
    LaunchedEffect(idNodo) { idNodo?.let { salidaVM.cargarDeNodo(it) } }

    Pantalla(titulo = "Solicitudes de visita", onVolver = { navController.popBackStack() }, viewModels = listOf(salidaVM)) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(solicitudes, key = { it.idSalida }) { s ->
                TarjetaSalida(s) {
                    if (s.estadoSalida == EstadosSalida.PENDIENTE && idNodo != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { salidaVM.responderSolicitud(s, confirmar = true, idNodo = idNodo) },
                                colors = ButtonDefaults.buttonColors(containerColor = Paleta.Verde),
                                modifier = Modifier.weight(1f)
                            ) { Text("Confirmar visita") }
                            OutlinedButton(
                                onClick = { salidaVM.responderSolicitud(s, confirmar = false, idNodo = idNodo) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Rechazar", color = Paleta.Rojo) }
                        }
                    }
                }
            }
            if (solicitudes.isEmpty()) item { MensajeVacio("No hay solicitudes de visitas grupales para tu templo", Icons.Default.Groups) }
        }
    }
}


// ---------- Vista previa para el panel Split de Android Studio (usa datos de prueba) ----------
@Preview(name = "Solicitudes de visita", showBackground = true, showSystemUi = true)
@Composable
private fun SolicitudesVisitaScreenPreview() {
    val vm = rememberViewModelsPrueba(rol = Roles.ENCARGADO) {
        usuario?.idNodoAsignado?.let { salida.cargarDeNodo(it) }
    }
    VistaPrevia { SolicitudesVisitaScreen(rememberNavController(), vm.auth, vm.salida) }
}
