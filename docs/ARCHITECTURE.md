# Technical Architecture
## FriLuft
**Version:** 1.0  
**Date:** 2026-07-07  
**Author:** Ali Abdullah

---

## 1. Architecture Pattern

**MVVM + Clean Architecture**

The app is split into three independent layers. Each layer only communicates with the layer directly below it. UI never touches the database or the network. Business logic never touches Android framework classes.

```
┌─────────────────────────────────────────────┐
│              Presentation Layer             │
│   Composables → ViewModels → UI State       │
├─────────────────────────────────────────────┤
│               Domain Layer                  │
│   Use Cases → Repository Interfaces → Models│
├─────────────────────────────────────────────┤
│                Data Layer                   │
│   SMHI API (Retrofit) ←→ Room (cache)       │
│   Repository Implementation                 │
└─────────────────────────────────────────────┘
```

### Key Rules
- `Composables` only observe `UiState` from `ViewModel` — no direct data access
- `ViewModels` only call `UseCases` — never DAOs or Retrofit directly
- `UseCases` contain all business logic — they are pure Kotlin, fully testable
- `Repositories` abstract the data source — ViewModel doesn't know if data came from Room or the network
- `Room` is the source of truth for current weather — SMHI is fetched when cache is stale

---

## 2. Package Structure

```
se.w3footprint.friluft/
│
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── WeatherDao.kt
│   │   ├── database/
│   │   │   └── FriLuftDatabase.kt
│   │   ├── entity/
│   │   │   └── WeatherCacheEntity.kt
│   │   └── store/
│   │       └── CityPreferencesStore.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── SmhiApi.kt
│   │   └── dto/
│   │       ├── SmhiResponseDto.kt
│   │       └── SmhiMapper.kt
│   └── repository/
│       └── WeatherRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Weather.kt          # CurrentWeather, HourlyForecast, DailyForecast, OutdoorScore
│   │   └── City.kt             # City data class + SWEDISH_CITIES list
│   ├── repository/
│   │   └── WeatherRepository.kt
│   └── usecase/
│       ├── weather/
│       │   ├── GetCurrentWeatherUseCase.kt
│       │   └── GetForecastUseCase.kt   # GetHourlyForecastUseCase + GetDailyForecastUseCase
│       └── score/
│           └── GetOutdoorScoreUseCase.kt
│
├── presentation/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   ├── forecast/
│   │   ├── ForecastScreen.kt
│   │   ├── ForecastViewModel.kt
│   │   └── ForecastUiState.kt
│   ├── search/
│   │   ├── SearchScreen.kt
│   │   └── SearchViewModel.kt
│   ├── navigation/
│   │   ├── FriLuftNavGraph.kt
│   │   └── Screen.kt
│   └── common/
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
│
└── di/
    ├── NetworkModule.kt
    ├── DatabaseModule.kt
    └── RepositoryModule.kt
```

---

## 3. Data Flow

### Current weather (Home screen)

```
HomeScreen
  → HomeViewModel (init observes CityPreferencesStore)
    → GetCurrentWeatherUseCase(lat, lon)
      → WeatherRepositoryImpl.getCurrentWeather()
        → WeatherDao.getCache(key)           ← cache hit? emit immediately
        → SmhiApi.getForecast(lon, lat)      ← cache miss: fetch from SMHI
          → SmhiResponseDto.toCurrentWeather()
            → WeatherDao.insertCache()       ← update Room
              → emit Result.success(weather)
```

On network failure with a valid (possibly stale) cache, the stale data is returned rather than an error. On network failure with no cache, the error is surfaced to the UI.

### GPS location

```
HomeViewModel.onLocationPermissionGranted()
  → FusedLocationProviderClient.getCurrentLocation().await()
    → Geocoder.getFromLocation() → cityName (Swedish locale)
      → loadWeatherForCity(lat, lon, cityName)
```

ForecastViewModel independently fetches GPS — it is a separate screen with its own lifecycle and does not share ViewModel state with Home.

### City selection (Search → Home)

```
SearchScreen.onCitySelected(city)
  → SearchViewModel.onCitySelected()
    → CityPreferencesStore.saveCity(city)    ← DataStore write
      → HomeViewModel.observeSavedCity()     ← Flow collect triggers reload
        → loadWeatherForCity(city.lat, city.lon, city.name)
```

The `HomeViewModel` observes `CityPreferencesStore.lastCity` as a `Flow`. When the Search screen saves a city, the Home screen reacts automatically without any direct coupling between the two ViewModels.

---

## 4. Caching Strategy

**Network-first with TTL fallback**

| Scenario | Behaviour |
|---|---|
| Cache age < 30 min | Serve cache, skip network |
| Cache age ≥ 30 min, network OK | Fetch SMHI, update cache, serve fresh data |
| Cache age ≥ 30 min, network fail, cache exists | Serve stale cache (no error) |
| Cache age ≥ 30 min, network fail, no cache | Surface error to UI |

Cache key format: `"%.2f_%.2f".format(lat, lon)` — rounds to ~1km grid.

Hourly and daily forecasts are always fetched from the network (not cached).

---

## 5. SMHI Integration

**Base URL:** `https://opendata-download-metfcst.smhi.se/`

**Endpoint:**
```
GET api/category/pmp3g/version/2/geotype/point/lon/{lon}/lat/{lat}/data.json
```

Note: SMHI expects **longitude first**, then latitude in the URL path.

**Parsing:** Moshi with `@JsonClass(generateAdapter = true)` on all DTOs. KSP generates adapters at compile time — no reflection at runtime.

**Feels-like calculation:**
- Wind chill (temp ≤ 10°C, wind ≥ 1.3 m/s): Swedish/EU standard formula
- Heat index (temp ≥ 27°C): simplified Steadman formula
- Otherwise: actual temperature

---

## 6. Database

**Room database name:** `friluft.db`  
**Current version:** 1

### Table: `weather_cache`

| Column | Type | Description |
|---|---|---|
| `cacheKey` | TEXT (PK) | `"lat_lon"` rounded to 2 decimal places |
| `temperature` | REAL | °C |
| `feelsLike` | REAL | °C (calculated) |
| `windSpeed` | REAL | m/s |
| `windDirection` | REAL | degrees |
| `precipitation` | REAL | mm/h |
| `humidity` | INTEGER | % |
| `weatherSymbol` | INTEGER | SMHI Wsymb2 (1–27) |
| `visibility` | REAL | km |
| `updatedAtEpochSecond` | INTEGER | SMHI `approvedTime` as epoch |
| `cachedAtEpochSecond` | INTEGER | Device time when cached |

Stale entries (older than 2 hours) are evicted on each successful fetch.

**DataStore:** `city_prefs` — stores `lastCityName`, `lastCityLat`, `lastCityLon` as Preferences DataStore keys.

---

## 7. Navigation

```
NavHost (startDestination = home)
├── home
│   ├── → forecast
│   └── → search
├── forecast
│   └── ← popBackStack
└── search
    └── ← popBackStack
```

No bottom navigation bar — the app is shallow enough (3 screens) that a top app bar with a search icon and back arrow is sufficient.

---

## 8. Dependency Injection (Hilt)

| Module | Provides |
|---|---|
| `NetworkModule` | `Moshi`, `OkHttpClient`, `Retrofit`, `SmhiApi` |
| `DatabaseModule` | `FriLuftDatabase`, `WeatherDao` |
| `RepositoryModule` | `WeatherRepository` (bound to `WeatherRepositoryImpl`) |

`CityPreferencesStore` is provided directly via `@Inject constructor` with `@ApplicationContext`.

All ViewModels use `@HiltViewModel`. All use cases are plain classes with constructor injection.

---

## 9. Build Variants

| Variant | App ID suffix | Description |
|---|---|---|
| debug | `.debug` | OkHttp logging enabled, BuildConfig.DEBUG = true |
| release | — | Minified (R8), resources shrunk, no logging |

---

## 10. Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| Build system | AGP | 8.7.3 |
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| DI | Hilt | 2.54 |
| Database | Room | 2.6.1 |
| Networking | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| JSON | Moshi (KSP codegen) | 1.15.1 |
| Preferences | DataStore | 1.1.7 |
| Location | FusedLocationProvider | 21.3.0 |
| Coroutines | kotlinx.coroutines | 1.10.2 |
| Code gen | KSP | 2.0.21-1.0.28 |

---

## 11. Testing Strategy

| Layer | Tool | What is tested |
|---|---|---|
| Use Cases | JUnit 4 + MockK | Outdoor score thresholds, edge cases |
| Repository | JUnit 4 + MockK | Cache TTL logic, network/cache fallback |
| ViewModel | JUnit 4 + Turbine | UiState transitions, error handling |
| UI | Compose UI Test | GPS permission flow, city search, navigation |
