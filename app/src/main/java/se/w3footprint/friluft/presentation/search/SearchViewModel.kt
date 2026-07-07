package se.w3footprint.friluft.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.w3footprint.friluft.data.local.store.CityPreferencesStore
import se.w3footprint.friluft.domain.model.City
import se.w3footprint.friluft.domain.model.SWEDISH_CITIES
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<City> = SWEDISH_CITIES,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cityPreferencesStore: CityPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                results = if (query.isBlank()) SWEDISH_CITIES
                else SWEDISH_CITIES.filter { city -> city.name.contains(query, ignoreCase = true) }
            )
        }
    }

    fun onCitySelected(city: City) {
        viewModelScope.launch { cityPreferencesStore.saveCity(city) }
    }
}
