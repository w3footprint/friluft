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
import se.w3footprint.friluft.domain.model.WeatherResult
import se.w3footprint.friluft.domain.repository.WeatherRepository
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import javax.inject.Inject

private const val CACHE_TTL_SECONDS = 30 * 60L

class WeatherRepositoryImpl @Inject constructor(
    private val api: SmhiApi,
    private val weatherDao: WeatherDao,
) : WeatherRepository {

    override fun getCurrentWeather(lat: Double, lon: Double): Flow<WeatherResult<CurrentWeather>> = flow {
        val key = cacheKey(lat, lon)
        val now = Instant.now().epochSecond
        val cached = weatherDao.getCache(key)

        if (cached != null && now - cached.cachedAtEpochSecond < CACHE_TTL_SECONDS) {
            emit(WeatherResult.Fresh(cached.toDomain()))
            return@flow
        }

        try {
            val weather = api.getForecast(formatCoord(lon), formatCoord(lat)).toCurrentWeather()
            weatherDao.insertCache(weather.toEntity(key, now))
            weatherDao.evictOlderThan(now - CACHE_TTL_SECONDS * 4)
            emit(WeatherResult.Fresh(weather))
        } catch (e: UnknownHostException) {
            if (cached != null) emit(WeatherResult.Cached(cached.toDomain()))
            else emit(WeatherResult.Offline())
        } catch (e: Exception) {
            if (cached != null) emit(WeatherResult.Cached(cached.toDomain()))
            else emit(WeatherResult.Error(e.message))
        }
    }

    override fun getHourlyForecast(lat: Double, lon: Double): Flow<WeatherResult<List<HourlyForecast>>> = flow {
        try {
            val data = api.getForecast(formatCoord(lon), formatCoord(lat)).toHourlyForecast()
            emit(WeatherResult.Fresh(data))
        } catch (e: UnknownHostException) {
            emit(WeatherResult.Offline())
        } catch (e: Exception) {
            emit(WeatherResult.Error(e.message))
        }
    }

    override fun getDailyForecast(lat: Double, lon: Double): Flow<WeatherResult<List<DailyForecast>>> = flow {
        try {
            val data = api.getForecast(formatCoord(lon), formatCoord(lat)).toDailyForecast()
            emit(WeatherResult.Fresh(data))
        } catch (e: UnknownHostException) {
            emit(WeatherResult.Offline())
        } catch (e: Exception) {
            emit(WeatherResult.Error(e.message))
        }
    }

    private fun formatCoord(coord: Double): String = String.format(Locale.US, "%.6f", coord)
    private fun cacheKey(lat: Double, lon: Double) = "%.2f_%.2f".format(Locale.US, lat, lon)

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
