package se.w3footprint.friluft.data.local.store

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastKnownLocation @Inject constructor() {
    var lat: Double? = null
    var lon: Double? = null
}
