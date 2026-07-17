package se.w3footprint.friluft.domain.usecase.score

import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.OutdoorScore
import javax.inject.Inject

fun outdoorRating(temp: Double, wind: Double, precip: Double): OutdoorScore.Rating = when {
    precip >= 2.0 || wind >= 10.0 || temp < -10.0 -> OutdoorScore.Rating.STAY_INSIDE
    precip >= 0.5 || wind >= 7.0 || temp < -5.0 || temp > 32.0 -> OutdoorScore.Rating.OKAY
    else -> OutdoorScore.Rating.GOOD
}

class GetOutdoorScoreUseCase @Inject constructor() {
    operator fun invoke(weather: CurrentWeather) = OutdoorScore(
        rating = outdoorRating(weather.temperature, weather.windSpeed, weather.precipitation),
        temp = weather.temperature,
        wind = weather.windSpeed,
        precip = weather.precipitation,
    )
}
