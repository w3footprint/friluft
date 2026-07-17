package se.w3footprint.friluft.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import se.w3footprint.friluft.R
import se.w3footprint.friluft.domain.model.CurrentWeather
import se.w3footprint.friluft.domain.model.OutdoorScore
import se.w3footprint.friluft.presentation.common.theme.GrassGreen
import se.w3footprint.friluft.presentation.common.theme.StormRed
import se.w3footprint.friluft.presentation.common.theme.SunYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToForecast: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onLocationPermissionGranted()
        else viewModel.onLocationPermissionDenied()
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = uiState.cityName.ifEmpty { stringResource(R.string.app_name) },
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { toggleLanguage() }) {
                        Icon(Icons.Default.Language, contentDescription = stringResource(R.string.change_language))
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_city))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.locationPermissionRequired && uiState.weather == null -> LocationPermissionPrompt(
                    modifier = Modifier.align(Alignment.Center),
                    onRetry = {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                )
                uiState.isOffline && uiState.weather == null -> OfflineEmptyScreen(
                    modifier = Modifier.align(Alignment.Center),
                    onRetry = { viewModel.onLocationPermissionGranted() }
                )
                uiState.error != null && uiState.weather == null -> Text(
                    text = uiState.error ?: "",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                uiState.weather != null -> HomeContent(
                    weather = uiState.weather!!,
                    score = uiState.outdoorScore,
                    isShowingCachedData = uiState.isShowingCachedData,
                    onViewForecast = onNavigateToForecast,
                )
            }
        }
    }
}

private fun toggleLanguage() {
    val current = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (current.isEmpty) java.util.Locale.getDefault().language else current[0]?.language
    val next = if (currentTag == "sv") "en" else "sv"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
}

@Composable
private fun HomeContent(
    weather: CurrentWeather,
    score: OutdoorScore?,
    isShowingCachedData: Boolean,
    onViewForecast: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isShowingCachedData) OfflineBanner()
        TemperatureHero(weather = weather)
        score?.let { OutdoorScoreCard(score = it) }
        WeatherDetailsCard(weather = weather)
        Button(onClick = onViewForecast, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.forecast_button))
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OfflineBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.offline_banner),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OfflineEmptyScreen(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = Icons.Default.SignalWifiOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        )
        Text(
            text = stringResource(R.string.offline_no_cache_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.offline_no_cache_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun TemperatureHero(weather: CurrentWeather) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                ),
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${weather.temperature.toInt()}°", fontSize = 80.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = stringResource(R.string.feels_like, weather.feelsLike.toInt().toString()),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                text = weatherSymbolLabel(weather.weatherSymbol),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun OutdoorScoreCard(score: OutdoorScore) {
    val (containerColor, contentColor) = when (score.rating) {
        OutdoorScore.Rating.GOOD -> GrassGreen to Color.White
        OutdoorScore.Rating.OKAY -> SunYellow to Color.Black
        OutdoorScore.Rating.STAY_INSIDE -> StormRed to Color.White
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = score.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
            Text(text = score.reason, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun WeatherDetailsCard(weather: CurrentWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            DetailItem(
                icon = Icons.Default.Air,
                label = stringResource(R.string.wind),
                value = stringResource(R.string.wind_value, weather.windSpeed.toInt().toString()),
            )
            DetailItem(
                icon = Icons.Default.WaterDrop,
                label = stringResource(R.string.precipitation),
                value = stringResource(R.string.precip_value, weather.precipitation.toString()),
            )
            DetailItem(
                icon = Icons.Default.WaterDrop,
                label = stringResource(R.string.humidity),
                value = stringResource(R.string.humidity_value, weather.humidity.toString()),
            )
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun LocationPermissionPrompt(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(modifier = modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.location_required_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.location_required_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Button(onClick = onRetry) { Text(stringResource(R.string.allow_location)) }
    }
}

@Composable
fun weatherSymbolLabel(symbol: Int): String = when (symbol) {
    1 -> stringResource(R.string.symbol_clear)
    2 -> stringResource(R.string.symbol_nearly_clear)
    3, 4 -> stringResource(R.string.symbol_half_cloudy)
    5, 6 -> stringResource(R.string.symbol_cloudy)
    7 -> stringResource(R.string.symbol_fog)
    8, 9, 10 -> stringResource(R.string.symbol_rain_showers)
    11 -> stringResource(R.string.symbol_thunder)
    12, 13, 14 -> stringResource(R.string.symbol_sleet_showers)
    15, 16, 17 -> stringResource(R.string.symbol_snow_showers)
    18, 19, 20 -> stringResource(R.string.symbol_rain)
    21 -> stringResource(R.string.symbol_thunder_rain)
    22, 23, 24 -> stringResource(R.string.symbol_sleet)
    25, 26, 27 -> stringResource(R.string.symbol_snow)
    else -> stringResource(R.string.symbol_unknown)
}
