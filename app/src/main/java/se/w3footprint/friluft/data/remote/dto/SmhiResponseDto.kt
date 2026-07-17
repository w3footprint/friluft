package se.w3footprint.friluft.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmhiResponseDto(
    @Json(name = "createdTime") val createdTime: String,
    @Json(name = "referenceTime") val referenceTime: String,
    @Json(name = "timeSeries") val timeSeries: List<TimeSeriesDto>,
)

@JsonClass(generateAdapter = true)
data class TimeSeriesDto(
    @Json(name = "time") val time: String,
    @Json(name = "data") val data: ForecastDataDto,
)

@JsonClass(generateAdapter = true)
data class ForecastDataDto(
    @Json(name = "air_temperature") val airTemperature: Double,
    @Json(name = "wind_speed") val windSpeed: Double,
    @Json(name = "wind_from_direction") val windDirection: Double,
    @Json(name = "precipitation_amount_mean") val precipitationAmount: Double,
    @Json(name = "relative_humidity") val humidity: Double,
    @Json(name = "symbol_code") val symbolCode: Int,
    @Json(name = "visibility_in_air") val visibility: Double,
)
