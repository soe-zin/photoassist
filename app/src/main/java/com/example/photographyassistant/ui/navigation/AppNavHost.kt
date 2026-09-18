package com.example.photographyassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.photographyassistant.ui.compose.MainScreen
import com.example.photographyassistant.ui.screens.ExposureScreen
import com.example.photographyassistant.ui.screens.LuxMeterScreen
import com.example.photographyassistant.ui.screens.SolarCalculatorScreen
import com.example.photographyassistant.ui.screens.UtilitiesScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen()
        }
        composable("exposure") {
            ExposureScreen()
        }
        composable("lux_meter") {
            LuxMeterScreen()
        }
        composable("solar_calculator") {
            SolarCalculatorScreen()
        }
        composable("utilities") {
            UtilitiesScreen()
        }
    }
}
