package se.w3footprint.friluft.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SmhiResponseDto(
    @Json(name = "approvedTime") val approvedTime: String,
    @Json(name = "referenceTime") val referenceTime: String,
    @Json(name = "timeSeries") val timeSeries: List<TimeSeriesDto>,
)

@JsonClass(generateAdapter = true)
data class TimeSeriesDto(
    @Json(name = "validTime") val validTime: String,
    @Json(name = "parameters") val parameters: List<ParameterDto>,
)

@JsonClass(generateAdapter = true)
data class ParameterDto(
    @Json(name = "name") val name: String,
    @Json(name = "unit") val unit: String,
    @Json(name = "values") val values: List<Double>,
)
