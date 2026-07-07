package se.w3footprint.friluft.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import se.w3footprint.friluft.data.local.dao.WeatherDao
import se.w3footprint.friluft.data.local.entity.WeatherCacheEntity

@Database(entities = [WeatherCacheEntity::class], version = 1, exportSchema = false)
abstract class FriLuftDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
}
