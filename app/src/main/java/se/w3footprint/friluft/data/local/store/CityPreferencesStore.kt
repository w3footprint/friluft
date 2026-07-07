package se.w3footprint.friluft.data.local.store

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import se.w3footprint.friluft.domain.model.City
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "city_prefs")

@Singleton
class CityPreferencesStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyName = stringPreferencesKey("last_city_name")
    private val keyLat = doublePreferencesKey("last_city_lat")
    private val keyLon = doublePreferencesKey("last_city_lon")

    val lastCity: Flow<City?> = context.dataStore.data.map { prefs ->
        val name = prefs[keyName] ?: return@map null
        val lat = prefs[keyLat] ?: return@map null
        val lon = prefs[keyLon] ?: return@map null
        City(name = name, lat = lat, lon = lon)
    }

    suspend fun saveCity(city: City) {
        context.dataStore.edit { prefs ->
            prefs[keyName] = city.name
            prefs[keyLat] = city.lat
            prefs[keyLon] = city.lon
        }
    }
}
