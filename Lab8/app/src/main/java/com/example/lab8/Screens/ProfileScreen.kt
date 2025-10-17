package com.example.lab8.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lab8.R

@Composable
fun ProfileScreen(navController: NavController) {
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Avatar",
            modifier = Modifier.size(100.dp)
        )

        Spacer(Modifier.height(16.dp))
        Text("Nombre: Marcela Castillo")
        Text("Email: marcela@example.com")

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tema oscuro")
            Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
        Spacer(Modifier.height(24.dp))


    }
}