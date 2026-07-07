package se.w3footprint.friluft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String,
    val temperature: Double,
    val feelsLike: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val precipitation: Double,
    val humidity: Int,
    val weatherSymbol: Int,
    val visibility: Double,
    val updatedAtEpochSecond: Long,
    val cachedAtEpochSecond: Long,
)
