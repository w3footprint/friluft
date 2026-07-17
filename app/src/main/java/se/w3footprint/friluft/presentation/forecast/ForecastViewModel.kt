package se.w3footprint.friluft.presentation.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.friluft.domain.model.WeatherResult
import se.w3footprint.friluft.domain.usecase.weather.GetDailyForecastUseCase
import se.w3footprint.friluft.domain.usecase.weather.GetHourlyForecastUseCase
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getDailyForecast: GetDailyForecastUseCase,
    private val getHourlyForecast: GetHourlyForecastUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    fun reload() {
        val state = _uiState.value
        if (state.lastLat != null && state.lastLon != null) {
            loadForecastForCity(state.lastLat, state.lastLon)
        }
    }

    fun loadForecastForCity(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isOffline = false, lastLat = lat, lastLon = lon) }
            launch {
                getDailyForecast(lat, lon).collect { result ->
                    when (result) {
                        is WeatherResult.Fresh -> _uiState.update { it.copy(daily = result.data) }
                        is WeatherResult.Offline -> _uiState.update { it.copy(isOffline = true) }
                        is WeatherResult.Error -> _uiState.update { it.copy(error = result.message) }
                        else -> Unit
                    }
                }
            }
            launch {
                getHourlyForecast(lat, lon).collect { result ->
                    when (result) {
                        is WeatherResult.Fresh -> _uiState.update { it.copy(hourly = result.data, isLoading = false) }
                        is WeatherResult.Offline -> _uiState.update { it.copy(isLoading = false, isOffline = true) }
                        is WeatherResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                        else -> Unit
                    }
                }
            }
        }
    }
}
