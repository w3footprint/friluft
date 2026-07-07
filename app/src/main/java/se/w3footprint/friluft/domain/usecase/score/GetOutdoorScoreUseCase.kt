package se.w3footprint.friluft.domain.usecase.score

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import se.w3footprint.friluft.R
import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.OutdoorScore
import javax.inject.Inject

class GetOutdoorScoreUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    operator fun invoke(weather: CurrentWeather): OutdoorScore {
        val temp = weather.temperature
        val wind = weather.windSpeed
        val precip = weather.precipitation

        return when {
            precip >= 2.0 -> OutdoorScore(
                OutdoorScore.Rating.STAY_INSIDE,
                context.getString(R.string.score_stay_inside_label),
                context.getString(R.string.score_heavy_rain, precip.toInt().toString()),
            )
            wind >= 10.0 -> OutdoorScore(
                OutdoorScore.Rating.STAY_INSIDE,
                context.getString(R.string.score_stay_inside_label),
                context.getString(R.string.score_strong_wind, wind.toInt().toString()),
            )
            temp < -10.0 -> OutdoorScore(
                OutdoorScore.Rating.STAY_INSIDE,
                context.getString(R.string.score_stay_inside_label),
                context.getString(R.string.score_extreme_cold, temp.toInt().toString()),
            )
            precip in 0.5..1.9 || wind in 7.0..9.9 || temp < -5.0 || temp > 32.0 -> OutdoorScore(
                OutdoorScore.Rating.OKAY,
                context.getString(R.string.score_okay_label),
                buildOkayReason(temp, wind, precip),
            )
            else -> OutdoorScore(
                OutdoorScore.Rating.GOOD,
                context.getString(R.string.score_good_label),
                context.getString(R.string.score_good_reason),
            )
        }
    }

    private fun buildOkayReason(temp: Double, wind: Double, precip: Double): String = when {
        precip >= 0.5 -> context.getString(R.string.score_light_rain, precip.toInt().toString())
        wind >= 7.0 -> context.getString(R.string.score_windy, wind.toInt().toString())
        temp < -5.0 -> context.getString(R.string.score_cold, temp.toInt().toString())
        temp > 32.0 -> context.getString(R.string.score_hot, temp.toInt().toString())
        else -> context.getString(R.string.score_good_reason)
    }
}
