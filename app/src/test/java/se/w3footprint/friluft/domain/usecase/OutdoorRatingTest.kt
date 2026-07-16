package se.w3footprint.friluft.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import se.w3footprint.friluft.domain.model.OutdoorScore
import se.w3footprint.friluft.domain.usecase.score.outdoorRating

class OutdoorRatingTest {

    // GOOD conditions

    @Test
    fun `perfect conditions returns GOOD`() {
        assertEquals(OutdoorScore.Rating.GOOD, outdoorRating(temp = 18.0, wind = 3.0, precip = 0.0))
    }

    @Test
    fun `warm calm dry day returns GOOD`() {
        assertEquals(OutdoorScore.Rating.GOOD, outdoorRating(temp = 25.0, wind = 6.9, precip = 0.4))
    }

    @Test
    fun `borderline temp just above minus 5 returns GOOD`() {
        assertEquals(OutdoorScore.Rating.GOOD, outdoorRating(temp = -4.9, wind = 3.0, precip = 0.0))
    }

    // OKAY conditions

    @Test
    fun `light rain tips to OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = 15.0, wind = 3.0, precip = 0.5))
    }

    @Test
    fun `wind at 7 ms tips to OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = 15.0, wind = 7.0, precip = 0.0))
    }

    @Test
    fun `temperature below minus 5 returns OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = -5.1, wind = 3.0, precip = 0.0))
    }

    @Test
    fun `temperature above 32 returns OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = 32.1, wind = 3.0, precip = 0.0))
    }

    // STAY_INSIDE conditions

    @Test
    fun `heavy rain returns STAY_INSIDE`() {
        assertEquals(OutdoorScore.Rating.STAY_INSIDE, outdoorRating(temp = 15.0, wind = 3.0, precip = 2.0))
    }

    @Test
    fun `strong wind at 10 ms returns STAY_INSIDE`() {
        assertEquals(OutdoorScore.Rating.STAY_INSIDE, outdoorRating(temp = 15.0, wind = 10.0, precip = 0.0))
    }

    @Test
    fun `extreme cold below minus 10 returns STAY_INSIDE`() {
        assertEquals(OutdoorScore.Rating.STAY_INSIDE, outdoorRating(temp = -10.1, wind = 3.0, precip = 0.0))
    }

    @Test
    fun `storm with heavy rain and wind returns STAY_INSIDE`() {
        assertEquals(OutdoorScore.Rating.STAY_INSIDE, outdoorRating(temp = 12.0, wind = 12.0, precip = 5.0))
    }

    // Boundary precision

    @Test
    fun `precip just below heavy rain threshold stays OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = 15.0, wind = 3.0, precip = 1.9))
    }

    @Test
    fun `wind just below storm threshold stays OKAY`() {
        assertEquals(OutdoorScore.Rating.OKAY, outdoorRating(temp = 15.0, wind = 9.9, precip = 0.0))
    }
}
