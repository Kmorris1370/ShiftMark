package com.example.shiftmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shiftmark.ui.LoginScreen
import com.example.shiftmark.ui.TimelineScreen
import com.example.shiftmark.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
                composable("login") {
                    LoginScreen(onLoginSuccess = {
                        navController.navigate("timeline")
                    })
                }
                composable("timeline") {
                    TimelineScreen(onOpenSettings = {
                        navController.navigate("settings")
                    })
                }
                composable("settings") {
                    SettingsScreen(onBack = {
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}