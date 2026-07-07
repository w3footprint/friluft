package se.w3footprint.friluft.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.w3footprint.friluft.data.local.dao.WeatherDao
import se.w3footprint.friluft.data.local.entity.WeatherCacheEntity
import se.w3footprint.friluft.data.remote.api.SmhiApi
import se.w3footprint.friluft.data.remote.dto.toDailyForecast
import se.w3footprint.friluft.data.remote.dto.toCurrentWeather
import se.w3footprint.friluft.data.remote.dto.toHourlyForecast
import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import se.w3footprint.friluft.domain.repository.WeatherRepository
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject

private const val CACHE_TTL_SECONDS = 30 * 60L

class WeatherRepositoryImpl @Inject constructor(
    private val api: SmhiApi,
    private val weatherDao: WeatherDao,
) : WeatherRepository {

    override fun getCurrentWeather(lat: Double, lon: Double): Flow<Result<CurrentWeather>> = flow {
        val key = cacheKey(lat, lon)
        val now = Instant.now().epochSecond
        val cached = weatherDao.getCache(key)

        if (cached != null && now - cached.cachedAtEpochSecond < CACHE_TTL_SECONDS) {
            emit(Result.success(cached.toDomain()))
            return@flow
        }

        val result = runCatching { api.getForecast(lon, lat).toCurrentWeather() }
        result.onSuccess { weather ->
            weatherDao.insertCache(weather.toEntity(key, now))
            weatherDao.evictOlderThan(now - CACHE_TTL_SECONDS * 4)
        }
        if (result.isFailure && cached != null) {
            emit(Result.success(cached.toDomain()))
        } else {
            emit(result)
        }
    }

    override fun getHourlyForecast(lat: Double, lon: Double): Flow<Result<List<HourlyForecast>>> = flow {
        emit(runCatching { api.getForecast(lon, lat).toHourlyForecast() })
    }

    override fun getDailyForecast(lat: Double, lon: Double): Flow<Result<List<DailyForecast>>> = flow {
        emit(runCatching { api.getForecast(lon, lat).toDailyForecast() })
    }

    private fun cacheKey(lat: Double, lon: Double) = "%.2f_%.2f".format(lat, lon)

    private fun WeatherCacheEntity.toDomain() = CurrentWeather(
        temperature = temperature,
        feelsLike = feelsLike,
        windSpeed = windSpeed,
        windDirection = windDirection,
        precipitation = precipitation,
        humidity = humidity,
        weatherSymbol = weatherSymbol,
        visibility = visibility,
        updatedAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(updatedAtEpochSecond), ZoneOffset.UTC),
    )

    private fun CurrentWeather.toEntity(key: String, nowEpoch: Long) = WeatherCacheEntity(
        cacheKey = key,
        temperature = temperature,
        feelsLike = feelsLike,
        windSpeed = windSpeed,
        windDirection = windDirection,
        precipitation = precipitation,
        humidity = humidity,
        weatherSymbol = weatherSymbol,
        visibility = visibility,
        updatedAtEpochSecond = updatedAt.toEpochSecond(),
        cachedAtEpochSecond = nowEpoch,
    )
}
