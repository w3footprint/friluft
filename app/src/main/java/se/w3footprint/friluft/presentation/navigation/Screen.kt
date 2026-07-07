package se.w3footprint.friluft.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Forecast : Screen("forecast")
    data object Search : Screen("search")
}
