package se.w3footprint.friluft.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Forecast : Screen("forecast/{lat}/{lon}") {
        fun route(lat: Double, lon: Double) = "forecast/$lat/$lon"
    }
    data object Search : Screen("search")
}
