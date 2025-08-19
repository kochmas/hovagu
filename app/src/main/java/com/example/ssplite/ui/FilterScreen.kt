package com.example.ssplite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun FilterScreen(navController: NavController) {
    Column(Modifier.padding(16.dp)) {
        Text("Filter presets coming soon")
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
    }
}
