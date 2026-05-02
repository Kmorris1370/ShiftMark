package com.example.shiftmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shiftmark.ui.*

class MainActivity : ComponentActivity() {
    private val viewModel: TimestampViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()

            // If a PIN exists, go straight to login. Otherwise, set one up.
            val startDestination = if (SecureStorage.isPinSet(context)) "login" else "pinSetup"

            NavHost(navController = navController, startDestination = startDestination) {
                composable("pinSetup") {
                    PinSetupScreen(onPinSet = {
                        navController.navigate("timeline") {
                            popUpTo("pinSetup") { inclusive = true }
                        }
                    })
                }
                composable("login") {
                    LoginScreen(onLoginSuccess = {
                        navController.navigate("timeline") {
                            popUpTo("login") { inclusive = true }
                        }
                    })
                }
                composable("timeline") {
                    TimelineScreen(
                        viewModel = viewModel,
                        onOpenMenu = { navController.navigate("menu") },
                        onOpenTimestamp = { id -> navController.navigate("timestampDetail/$id") },
                        onAddManual = { navController.navigate("manualEntry") }
                    )
                }
                composable("timestampDetail/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: return@composable
                    TimestampDetailScreen(
                        id = id,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("menu") {
                    MenuScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onManual = { navController.navigate("manual") },
                        onDataDeleted = {
                            // PIN was reset by Delete Data — bounce to PIN setup.
                            navController.navigate("pinSetup") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable("manual") {
                    ManualScreen(onBack = { navController.popBackStack() })
                }
                composable("manualEntry") {
                    ManualEntryScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
