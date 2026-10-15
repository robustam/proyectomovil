package com.arzobispado.redpatrimonial.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
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
fun MapScreen(
    navController: NavController,
    isGuest: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }

    val listaNodos = remember {
        listOf(
            NodoPatrimonial(
                id = 1,
                nombre = "Catedral Metropolitana de Santiago",
                direccion = "Plaza de Armas s/n",
                categoria = "Religioso / Histórico",
                estadoOperativo = "Abierto",
                latitud = -33.4372,
                longitud = -70.6506,
                imagenUrl = ""
            ),
            NodoPatrimonial(
                id = 2,
                nombre = "Iglesia San Francisco",
                direccion = "Av. Libertador Bernardo O'Higgins 834",
                categoria = "Monumento Histórico",
                estadoOperativo = "Abierto",
                latitud = -33.4442,
                longitud = -70.6458,
                imagenUrl = ""
            ),
            NodoPatrimonial(
                id = 3,
                nombre = "Parroquia San Saturnino",
                direccion = "Santo Domingo 2772, Barrio Yungay",
                categoria = "Patrimonio Local",
                estadoOperativo = "Restauración",
                latitud = -33.4390,
                longitud = -70.6720,
                imagenUrl = ""
            ),
            NodoPatrimonial(
                id = 4,
                nombre = "Basílica del Salvador",
                direccion = "Huérfanos 1701, Santiago Centro",
                categoria = "Arquitectura Neogótica",
                estadoOperativo = "Visita Guiada",
                latitud = -33.4410,
                longitud = -70.6620,
                imagenUrl = ""
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Red Patrimonial",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isGuest) "Modo Invitado" else "Santiago, Chile",
                            fontSize = 12.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                },
                actions = {
                    if (isGuest) {
                        TextButton(
                            onClick = {
                                navController.navigate(AppScreens.Login.route) {
                                    popUpTo(AppScreens.Map.route) { inclusive = true }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Iniciar Sesión",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingresar", color = Color.White, fontSize = 12.sp)
                        }
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar iglesia, parroquia o monumento...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Text(
                text = "Puntos de Interés Patrimonial",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaNodos) { nodo ->
                    ItemNodoPatrimonial(
                        nodo = nodo,
                        isGuest = isGuest,
                        onClick = {
                            navController.navigate(AppScreens.Detail.createRoute(nodo.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemNodoPatrimonial(
    nodo: NodoPatrimonial,
    isGuest: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF2563EB)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nodo.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = nodo.direccion,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = nodo.categoria,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = if (nodo.estadoOperativo == "Abierto") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = nodo.estadoOperativo,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (nodo.estadoOperativo == "Abierto") Color(0xFF166534) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!isGuest) {
                IconButton(onClick = { /* Acción para usuarios autenticados */ }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Guardar Favorito",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MapScreenPreview() {
    MapScreen(navController = rememberNavController(), isGuest = true)
}