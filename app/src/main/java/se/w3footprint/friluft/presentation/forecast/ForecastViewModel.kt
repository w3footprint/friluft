package se.w3footprint.friluft.presentation.forecast

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import se.w3footprint.friluft.domain.usecase.weather.GetDailyForecastUseCase
import se.w3footprint.friluft.domain.usecase.weather.GetHourlyForecastUseCase
import javax.inject.Inject

@HiltViewModel
class ForecastViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDailyForecast: GetDailyForecastUseCase,
    private val getHourlyForecast: GetHourlyForecastUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForecastUiState())
    val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

    init {
        loadForecast()
    }

    fun loadForecastForCity(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            launch {
                getDailyForecast(lat, lon).collect { result ->
                    result.fold(
                        onSuccess = { daily -> _uiState.update { it.copy(daily = daily) } },
                        onFailure = { e -> _uiState.update { it.copy(error = e.message) } },
                    )
                }
            }
            launch {
                getHourlyForecast(lat, lon).collect { result ->
                    result.fold(
                        onSuccess = { hourly -> _uiState.update { it.copy(hourly = hourly, isLoading = false) } },
                        onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } },
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadForecast() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                val location = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                ).await() ?: return@launch
                loadForecastForCity(location.latitude, location.longitude)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
