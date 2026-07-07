package se.w3footprint.friluft.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    LaunchedEffect(Unit) {
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
                            text = uiState.cityName.ifEmpty { "FriLuft" },
                            modifier = Modifier.padding(start = 4.dp),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Sök stad")
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
                uiState.error != null && uiState.weather == null -> Text(
                    text = uiState.error ?: "",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                uiState.weather != null -> HomeContent(
                    weather = uiState.weather!!,
                    score = uiState.outdoorScore,
                    onViewForecast = onNavigateToForecast,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(weather: CurrentWeather, score: OutdoorScore?, onViewForecast: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TemperatureHero(weather = weather)
        score?.let { OutdoorScoreCard(score = it) }
        WeatherDetailsCard(weather = weather)
        Button(onClick = onViewForecast, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Visa 7-dagarsprognos")
        }
        Spacer(Modifier.height(16.dp))
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
            Text(text = "Känns som ${weather.feelsLike.toInt()}°", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f))
            Text(text = weatherSymbolLabel(weather.weatherSymbol), style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(top = 4.dp))
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
            DetailItem(icon = Icons.Default.Air, label = "Vind", value = "${weather.windSpeed.toInt()} m/s")
            DetailItem(icon = Icons.Default.WaterDrop, label = "Nederbörd", value = "${weather.precipitation} mm/h")
            DetailItem(icon = Icons.Default.WaterDrop, label = "Luftfuktighet", value = "${weather.humidity}%")
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
        Text("Platsbehörighet krävs", style = MaterialTheme.typography.titleLarge)
        Text("FriLuft behöver din plats för att visa väder.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Button(onClick = onRetry) { Text("Tillåt plats") }
    }
}

private fun weatherSymbolLabel(symbol: Int): String = when (symbol) {
    1 -> "Klart"; 2 -> "Nästan klart"; 3, 4 -> "Halvklart"; 5, 6 -> "Molnigt"
    7 -> "Dimma"; 8, 9, 10 -> "Regnskurar"; 11 -> "Åskväder"
    12, 13, 14 -> "Snöblandad regn"; 15, 16, 17 -> "Snöbyar"
    18, 19, 20 -> "Regn"; 21 -> "Åska med regn"
    22, 23, 24 -> "Snöblandat regn"; 25, 26, 27 -> "Snöfall"
    else -> "Okänt"
}
