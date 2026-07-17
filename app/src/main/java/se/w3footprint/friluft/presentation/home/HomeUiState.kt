package se.w3footprint.friluft.presentation.home

import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.OutdoorScore

data class HomeUiState(
    val isLoading: Boolean = false,
    val weather: CurrentWeather? = null,
    val outdoorScore: OutdoorScore? = null,
    val cityName: String = "",
    val error: String? = null,
    val isOffline: Boolean = false,
    val isShowingCachedData: Boolean = false,
    val locationPermissionRequired: Boolean = false,
)
