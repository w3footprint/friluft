package se.w3footprint.friluft.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import se.w3footprint.friluft.presentation.forecast.ForecastScreen
import se.w3footprint.friluft.presentation.home.HomeScreen
import se.w3footprint.friluft.presentation.search.SearchScreen

@Composable
fun FriLuftNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToForecast = { navController.navigate(Screen.Forecast.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
            )
        }
        composable(Screen.Forecast.route) {
            ForecastScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Search.route) {
            SearchScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
