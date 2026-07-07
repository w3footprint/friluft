package se.w3footprint.friluft.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
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
import se.w3footprint.friluft.data.local.store.CityPreferencesStore
import se.w3footprint.friluft.domain.usecase.score.GetOutdoorScoreUseCase
import se.w3footprint.friluft.domain.usecase.weather.GetCurrentWeatherUseCase
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCurrentWeather: GetCurrentWeatherUseCase,
    private val getOutdoorScore: GetOutdoorScoreUseCase,
    private val cityPreferencesStore: CityPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSavedCity()
    }

    private fun observeSavedCity() {
        viewModelScope.launch {
            cityPreferencesStore.lastCity.collect { city ->
                if (city != null) loadWeatherForCity(city.lat, city.lon, city.name)
            }
        }
    }

    fun onLocationPermissionGranted() = fetchWeatherForCurrentLocation()

    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(locationPermissionRequired = true) }
    }

    fun loadWeatherForCity(lat: Double, lon: Double, cityName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, cityName = cityName) }
            getCurrentWeather(lat, lon).collect { result ->
                result.fold(
                    onSuccess = { weather ->
                        _uiState.update {
                            it.copy(isLoading = false, weather = weather, outdoorScore = getOutdoorScore(weather))
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    },
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchWeatherForCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                val location = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token
                ).await()

                if (location == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Kunde inte hämta plats") }
                    return@launch
                }

                val cityName = resolveCityName(location.latitude, location.longitude)
                loadWeatherForCity(location.latitude, location.longitude, cityName)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun resolveCityName(lat: Double, lon: Double): String = try {
        val geocoder = Geocoder(context, Locale("sv", "SE"))
        @Suppress("DEPRECATION")
        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()?.locality ?: "Din plats"
    } catch (e: Exception) {
        "Din plats"
    }
}
