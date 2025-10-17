package com.example.lab8


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lab8.Screens.DetailsScreen
import com.example.lab8.Screens.HomeScreen
import com.example.lab8.Screens.ProfileScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(navController)
        }

        composable("details/{photoId}") { backStack ->
            val id = backStack.arguments?.getString("photoId")?.toLongOrNull()
            DetailsScreen(navController, photoId = id)
        }

        composable("profile") {
            ProfileScreen(navController)
        }
    }
}
