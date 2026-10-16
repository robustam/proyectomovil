package com.arzobispado.redpatrimonial.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.arzobispado.redpatrimonial.navigation.AppScreens

// Modelo de datos local para alertas de mantenimiento y conservación
data class AlertaComunitaria(
    val id: Int,
    val titulo: String,
    val templo: String,
    val estado: String,
    val fecha: String,
    val tipoIncidencia: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityAlertsScreen(
    navController: NavController,
    isGuest: Boolean = true
) {
    var showGuestDialog by remember { mutableStateOf(false) }

    val listaAlertas = remember {
        listOf(
            AlertaComunitaria(
                1,
                "Desprendimiento leve de estuco en fachada",
                "Iglesia San Francisco",
                "En revisión",
                "Hace 2 días",
                "Conservación"
            ),
            AlertaComunitaria(
                2,
                "Fallo en iluminación exterior nocturna",
                "Catedral Metropolitana",
                "En reparación",
                "Ayer",
                "Infraestructura"
            ),
            AlertaComunitaria(
                3,
                "Filtración de humedad en tejado lateral",
                "Parroquia San Saturnino",
                "En revisión",
                "Hace 5 días",
                "Mantenimiento"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportes de Conservación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isGuest) {
                        showGuestDialog = true
                    } else {
                        // Lógica para enviar nuevo reporte
                    }
                },
                containerColor = Color(0xFFDC2626),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            Text(
                text = "Avisos y Alertas Comunitarias",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaAlertas) { alerta ->
                    ItemAlerta(alerta = alerta)
                }
            }
        }
    }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("Función para Usuarios Registrados") },
            text = { Text("Debes iniciar sesión para emitir reportes de deterioro o mantenimiento en el patrimonio.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGuestDialog = false
                        navController.navigate(AppScreens.Login.route)
                    }
                ) {
                    Text("Iniciar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ItemAlerta(alerta: AlertaComunitaria) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = alerta.tipoIncidencia,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = if (alerta.estado == "En reparación") Color(0xFFFEF3C7) else Color(0xFFE0E7FF),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = alerta.estado,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alerta.estado == "En reparación") Color(0xFF92400E) else Color(0xFF3730A3),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alerta.titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Church,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alerta.templo,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = alerta.fecha,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CommunityAlertsScreenPreview() {
    CommunityAlertsScreen(navController = rememberNavController())
}