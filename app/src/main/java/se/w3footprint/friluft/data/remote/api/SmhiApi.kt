package se.w3footprint.friluft.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import se.w3footprint.friluft.data.remote.dto.SmhiResponseDto

interface SmhiApi {

    @GET("api/category/snow1g/version/1/geotype/point/lon/{lon}/lat/{lat}/data.json")
    suspend fun getForecast(
        @Path("lon") lon: String,
        @Path("lat") lat: String,
    ): SmhiResponseDto
}
