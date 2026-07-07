package se.w3footprint.friluft.domain.usecase.score

import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.OutdoorScore
import javax.inject.Inject

class GetOutdoorScoreUseCase @Inject constructor() {

    operator fun invoke(weather: CurrentWeather): OutdoorScore {
        val temp = weather.temperature
        val wind = weather.windSpeed
        val precip = weather.precipitation

        return when {
            precip >= 2.0 -> OutdoorScore(OutdoorScore.Rating.STAY_INSIDE, "Stanna inne", "Kraftigt regn (${precip.toInt()} mm/h)")
            wind >= 10.0 -> OutdoorScore(OutdoorScore.Rating.STAY_INSIDE, "Stanna inne", "Hård vind (${wind.toInt()} m/s)")
            temp < -10.0 -> OutdoorScore(OutdoorScore.Rating.STAY_INSIDE, "Stanna inne", "Extrem kyla (${temp.toInt()}°C)")
            precip in 0.5..1.9 || wind in 7.0..9.9 || temp < -5.0 || temp > 32.0 -> OutdoorScore(
                OutdoorScore.Rating.OKAY, "Går bra", buildOkayReason(temp, wind, precip)
            )
            else -> OutdoorScore(OutdoorScore.Rating.GOOD, "Perfekt utomhus", "Bra väder för friluftsliv")
        }
    }

    private fun buildOkayReason(temp: Double, wind: Double, precip: Double): String = when {
        precip >= 0.5 -> "Lite regn (${precip.toInt()} mm/h)"
        wind >= 7.0 -> "Blåsigt (${wind.toInt()} m/s)"
        temp < -5.0 -> "Kallt (${temp.toInt()}°C) — klä på dig"
        temp > 32.0 -> "Varmt (${temp.toInt()}°C) — ta det lugnt"
        else -> "Acceptabelt väder"
    }
}
