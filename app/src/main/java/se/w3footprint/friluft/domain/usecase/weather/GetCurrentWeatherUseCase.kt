package se.w3footprint.friluft.domain.usecase.weather

import kotlinx.coroutines.flow.Flow
import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(lat: Double, lon: Double): Flow<Result<CurrentWeather>> =
        repository.getCurrentWeather(lat, lon)
}
