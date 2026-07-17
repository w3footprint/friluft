package se.w3footprint.friluft.domain.repository

import kotlinx.coroutines.flow.Flow
import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import se.w3footprint.friluft.domain.model.WeatherResult

interface WeatherRepository {
    fun getCurrentWeather(lat: Double, lon: Double): Flow<WeatherResult<CurrentWeather>>
    fun getHourlyForecast(lat: Double, lon: Double): Flow<WeatherResult<List<HourlyForecast>>>
    fun getDailyForecast(lat: Double, lon: Double): Flow<WeatherResult<List<DailyForecast>>>
}
