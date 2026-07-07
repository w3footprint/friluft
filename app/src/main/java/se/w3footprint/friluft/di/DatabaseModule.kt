package se.w3footprint.friluft.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.w3footprint.friluft.data.local.dao.WeatherDao
import se.w3footprint.friluft.data.local.database.FriLuftDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FriLuftDatabase =
        Room.databaseBuilder(context, FriLuftDatabase::class.java, "friluft.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideWeatherDao(db: FriLuftDatabase): WeatherDao = db.weatherDao()
}
