package se.w3footprint.friluft.domain.model

sealed class WeatherResult<out T> {
    data class Fresh<T>(val data: T) : WeatherResult<T>()
    data class Cached<T>(val data: T) : WeatherResult<T>()
    data class Offline(val isNetworkError: Boolean = true) : WeatherResult<Nothing>()
    data class Error(val message: String?) : WeatherResult<Nothing>()
}
