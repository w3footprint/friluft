package se.w3footprint.friluft.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import se.w3footprint.friluft.data.local.entity.WeatherCacheEntity

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE cacheKey = :key LIMIT 1")
    suspend fun getCache(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(entity: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cachedAtEpochSecond < :before")
    suspend fun evictOlderThan(before: Long)
}
