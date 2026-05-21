package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WorkshopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: WorkshopViewModel = viewModel()
                val currentUser by viewModel.currentUser.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { user ->
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToCreateMr = {
                                    navController.navigate("create_mr")
                                },
                                onNavigateToMrList = {
                                    navController.navigate("mr_list")
                                },
                                onNavigateToAttendance = {
                                    navController.navigate("attendance")
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin_monitoring")
                                },
                                onNavigateToDetail = { mrNumber ->
                                    navController.navigate("mr_detail/$mrNumber")
                                }
                            )
                        }

                        composable("create_mr") {
                            MrFormScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("mr_list") {
                            MrListScreen(
                                        viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToDetail = { mrNumber ->
                                    navController.navigate("mr_detail/$mrNumber")
                                }
                            )
                        }

                        composable(
                            route = "mr_detail/{mrNumber}",
                            arguments = listOf(navArgument("mrNumber") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mrNumber = backStackEntry.arguments?.getString("mrNumber") ?: ""
                            MrDetailScreen(
                                viewModel = viewModel,
                                mrNumber = mrNumber,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("attendance") {
                            AttendanceScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("admin_monitoring") {
                            AdminMonitoringScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
