package se.w3footprint.friluft.domain.model

import java.time.ZonedDateTime

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val precipitation: Double,
    val humidity: Int,
    val weatherSymbol: Int,
    val visibility: Double,
    val updatedAt: ZonedDateTime,
)

data class HourlyForecast(
    val time: ZonedDateTime,
    val temperature: Double,
    val precipitation: Double,
    val windSpeed: Double,
    val weatherSymbol: Int,
)

data class DailyForecast(
    val date: ZonedDateTime,
    val tempMin: Double,
    val tempMax: Double,
    val precipitationSum: Double,
    val maxWindSpeed: Double,
    val dominantSymbol: Int,
)

data class OutdoorScore(
    val rating: Rating,
    val temp: Double,
    val wind: Double,
    val precip: Double,
) {
    enum class Rating { GOOD, OKAY, STAY_INSIDE }
}
