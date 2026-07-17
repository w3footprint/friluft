package se.w3footprint.friluft.data.remote.dto

import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.exp
import kotlin.math.pow

private val formatter = DateTimeFormatter.ISO_DATE_TIME

fun SmhiResponseDto.toCurrentWeather(): CurrentWeather {
    val now = timeSeries.first()
    val data = now.data
    val temp = data.airTemperature
    val wind = data.windSpeed
    val precip = data.precipitationAmount
    val humidity = data.humidity.toInt()
    val symbol = data.symbolCode
    val visibility = data.visibility
    val windDir = data.windDirection

    return CurrentWeather(
        temperature = temp,
        feelsLike = calculateFeelsLike(temp, wind, humidity),
        windSpeed = wind,
        windDirection = windDir,
        precipitation = precip,
        humidity = humidity,
        weatherSymbol = symbol,
        visibility = visibility,
        updatedAt = ZonedDateTime.parse(createdTime, formatter),
    )
}

fun SmhiResponseDto.toHourlyForecast(): List<HourlyForecast> =
    timeSeries.take(24).map { ts ->
        HourlyForecast(
            time = ZonedDateTime.parse(ts.time, formatter),
            temperature = ts.data.airTemperature,
            precipitation = ts.data.precipitationAmount,
            windSpeed = ts.data.windSpeed,
            weatherSymbol = ts.data.symbolCode,
        )
    }

fun SmhiResponseDto.toDailyForecast(): List<DailyForecast> {
    val byDay = timeSeries.groupBy {
        ZonedDateTime.parse(it.time, formatter).toLocalDate()
    }
    return byDay.entries.take(7).map { (_, entries) ->
        val temps = entries.map { it.data.airTemperature }
        val winds = entries.map { it.data.windSpeed }
        val precips = entries.map { it.data.precipitationAmount }
        val symbols = entries.map { it.data.symbolCode }
        DailyForecast(
            date = ZonedDateTime.parse(entries.first().time, formatter),
            tempMin = temps.min(),
            tempMax = temps.max(),
            precipitationSum = precips.sum(),
            maxWindSpeed = winds.max(),
            dominantSymbol = symbols.groupingBy { it }.eachCount().maxByOrNull { it.value }!!.key,
        )
    }
}

private fun calculateFeelsLike(temp: Double, wind: Double, humidity: Int): Double =
    when {
        temp <= 10.0 && wind >= 1.3 -> {
            val windKmh = (wind * 3.6).pow(0.16)
            13.12 + 0.6215 * temp - 11.37 * windKmh + 0.3965 * temp * windKmh
        }
        temp >= 27.0 -> {
            temp + (0.33 * (humidity / 100.0 * 6.105 * exp(17.27 * temp / (237.7 + temp)))) - 4.0
        }
        else -> temp
    }
