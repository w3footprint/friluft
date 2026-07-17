package se.w3footprint.friluft.presentation.forecast

import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast

data class ForecastUiState(
    val isLoading: Boolean = false,
    val daily: List<DailyForecast> = emptyList(),
    val hourly: List<HourlyForecast> = emptyList(),
    val error: String? = null,
    val isOffline: Boolean = false,
)
