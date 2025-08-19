package com.example.ssplite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ssplite.ui.PlayerScreen
import com.example.ssplite.ui.FilterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SSPLiteApp() }
    }
}

@Composable
fun SSPLiteApp() {
    val navController = rememberNavController()
    Surface {
        NavHost(navController = navController, startDestination = "player") {
            composable("player") { PlayerScreen(navController) }
            composable("filter") { FilterScreen(navController) }
        }
    }
}
