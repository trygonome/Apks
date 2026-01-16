package com.budgetvoice.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.budgetvoice.app.ui.screens.HomeScreen
import com.budgetvoice.app.ui.screens.VoiceInputScreen
import com.budgetvoice.app.ui.screens.CameraScanScreen
import com.budgetvoice.app.ui.viewmodel.ExpenseViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object VoiceInput : Screen("voice_input")
    object CameraScan : Screen("camera_scan")
}

@Composable
fun BudgetNavigation(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToVoice = { navController.navigate(Screen.VoiceInput.route) },
                onNavigateToCamera = { navController.navigate(Screen.CameraScan.route) }
            )
        }

        composable(Screen.VoiceInput.route) {
            VoiceInputScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CameraScan.route) {
            CameraScanScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
