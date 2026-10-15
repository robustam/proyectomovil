package com.arzobispado.redpatrimonial.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
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
import com.arzobispado.redpatrimonial.data.model.NodoPatrimonial
import com.arzobispado.redpatrimonial.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    nodoId: Int = 1,
    isGuest: Boolean = true
) {
    var isFavorite by remember { mutableStateOf(false) }
    var showGuestDialog by remember { mutableStateOf(false) }

    // Objeto simulado para el detalle del nodo seleccionado
    val nodo = remember {
        NodoPatrimonial(
            id = nodoId,
            nombre = "Catedral Metropolitana de Santiago",
            direccion = "Plaza de Armas s/n, Santiago Centro",
            categoria = "Religioso / Histórico",
            estadoOperativo = "Abierto",
            latitud = -33.4372,
            longitud = -70.6506,
            imagenUrl = ""
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle Patrimonial",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
                .verticalScroll(rememberScrollState())
        ) {
            // Contenedor de encabezado visual / Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1E3A8A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF93C5FD)
                )
            }

            // Información detallada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = nodo.categoria,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Botón de Favorito
                    IconButton(
                        onClick = {
                            if (isGuest) {
                                showGuestDialog = true
                            } else {
                                isFavorite = !isFavorite
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color(0xFFDC2626) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = nodo.nombre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = nodo.direccion,
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF166534),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Horarios: Lunes a Domingo de 09:00 a 19:00 hrs.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Reseña Histórica",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "La Catedral Metropolitana de Santiago es la sede de la Arquidiócesis de Santiago de Chile y el principal templo de la Iglesia católica en el país. Declarada Monumento Nacional, destaca por su arquitectura neoclásica y su rico patrimonio religioso e histórico.",
                    fontSize = 14.sp,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Botón para solicitar pase
                Button(
                    onClick = {
                        if (isGuest) {
                            showGuestDialog = true
                        } else {
                            // Lógica de generación de pase
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Pase de Visita",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Cuadro de diálogo que restringe acciones protegidas al Modo Invitado
    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("Función para Usuarios Registrados") },
            text = { Text("Debes iniciar sesión para guardar favoritos o solicitar pases de visita patrimoniales.") },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DetailScreenPreview() {
    DetailScreen(navController = rememberNavController())
}