package net.asere.omni.mvi.sample.main.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import net.asere.omni.mvi.sample.list.presentation.ListScreen
import net.asere.omni.mvi.sample.shared.presentation.navigation.NavigationRoutes

@Composable
fun MainNavGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.ListRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(NavigationRoutes.ListRoute) {
            ListScreen()
        }
    }
}