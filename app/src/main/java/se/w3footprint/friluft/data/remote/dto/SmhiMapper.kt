package se.w3footprint.friluft.data.remote.dto

import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.exp
import kotlin.math.pow

private val formatter = DateTimeFormatter.ISO_DATE_TIME

private fun TimeSeriesDto.param(name: String): Double =
    parameters.firstOrNull { it.name == name }?.values?.firstOrNull() ?: 0.0

fun SmhiResponseDto.toCurrentWeather(): CurrentWeather {
    val now = timeSeries.first()
    val temp = now.param("t")
    val wind = now.param("ws")
    val precip = now.param("pmean")
    val humidity = now.param("r").toInt()
    val symbol = now.param("Wsymb2").toInt()
    val visibility = now.param("vis")
    val windDir = now.param("wd")

    return CurrentWeather(
        temperature = temp,
        feelsLike = calculateFeelsLike(temp, wind, humidity),
        windSpeed = wind,
        windDirection = windDir,
        precipitation = precip,
        humidity = humidity,
        weatherSymbol = symbol,
        visibility = visibility,
        updatedAt = ZonedDateTime.parse(approvedTime, formatter),
    )
}

fun SmhiResponseDto.toHourlyForecast(): List<HourlyForecast> =
    timeSeries.take(24).map { ts ->
        HourlyForecast(
            time = ZonedDateTime.parse(ts.validTime, formatter),
            temperature = ts.param("t"),
            precipitation = ts.param("pmean"),
            windSpeed = ts.param("ws"),
            weatherSymbol = ts.param("Wsymb2").toInt(),
        )
    }

fun SmhiResponseDto.toDailyForecast(): List<DailyForecast> {
    val byDay = timeSeries.groupBy {
        ZonedDateTime.parse(it.validTime, formatter).toLocalDate()
    }
    return byDay.entries.take(7).map { (_, entries) ->
        val temps = entries.map { it.param("t") }
        val winds = entries.map { it.param("ws") }
        val precips = entries.map { it.param("pmean") }
        val symbols = entries.map { it.param("Wsymb2").toInt() }
        DailyForecast(
            date = ZonedDateTime.parse(entries.first().validTime, formatter),
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
