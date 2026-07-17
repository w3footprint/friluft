package se.w3footprint.friluft.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import se.w3footprint.friluft.presentation.forecast.ForecastScreen
import se.w3footprint.friluft.presentation.home.HomeScreen
import se.w3footprint.friluft.presentation.home.HomeViewModel
import se.w3footprint.friluft.presentation.search.SearchScreen

@Composable
fun FriLuftNavGraph(navController: NavHostController) {
    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToForecast = {
                    val lat = homeViewModel.lastLat ?: return@HomeScreen
                    val lon = homeViewModel.lastLon ?: return@HomeScreen
                    navController.navigate(Screen.Forecast.route(lat, lon))
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
            )
        }
        composable(
            route = Screen.Forecast.route,
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
            ),
        ) { backStack ->
            val lat = backStack.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStack.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            ForecastScreen(
                lat = lat,
                lon = lon,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
