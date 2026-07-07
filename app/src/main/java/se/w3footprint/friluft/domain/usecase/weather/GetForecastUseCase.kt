package se.w3footprint.friluft.domain.usecase.weather

import kotlinx.coroutines.flow.Flow
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import se.w3footprint.friluft.domain.repository.WeatherRepository
import javax.inject.Inject

class GetHourlyForecastUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(lat: Double, lon: Double): Flow<Result<List<HourlyForecast>>> =
        repository.getHourlyForecast(lat, lon)
}

class GetDailyForecastUseCase @Inject constructor(private val repository: WeatherRepository) {
    operator fun invoke(lat: Double, lon: Double): Flow<Result<List<DailyForecast>>> =
        repository.getDailyForecast(lat, lon)
}
