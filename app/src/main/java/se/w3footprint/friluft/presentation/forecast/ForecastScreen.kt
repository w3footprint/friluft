package se.w3footprint.friluft.presentation.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SignalWifiOff
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.w3footprint.friluft.R
import se.w3footprint.friluft.domain.model.DailyForecast
import se.w3footprint.friluft.domain.model.HourlyForecast
import se.w3footprint.friluft.presentation.home.weatherSymbolLabel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val hourFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(
    lat: Double,
    lon: Double,
    onNavigateBack: () -> Unit,
    viewModel: ForecastViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val dayFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())

    androidx.compose.runtime.LaunchedEffect(lat, lon) {
        viewModel.loadForecastForCity(lat, lon)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.forecast_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.isOffline && uiState.hourly.isEmpty() && uiState.daily.isEmpty() -> ForecastOfflineScreen(
                    modifier = Modifier.align(Alignment.Center),
                    onRetry = { viewModel.reload() },
                )
                uiState.error != null -> Text(
                    text = uiState.error ?: "",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                else -> ForecastContent(uiState = uiState, dayFormatter = dayFormatter)
            }
        }
    }
}

@Composable
private fun ForecastOfflineScreen(modifier: Modifier = Modifier, onRetry: () -> Unit) {
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
private fun ForecastContent(uiState: ForecastUiState, dayFormatter: DateTimeFormatter) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (uiState.hourly.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.hourly_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                HourlyChart(hourly = uiState.hourly)
            }
        }
        if (uiState.daily.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.weekly_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
            }
            items(uiState.daily) { day -> DailyForecastRow(day = day, dayFormatter = dayFormatter) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun HourlyChart(hourly: List<HourlyForecast>) {
    val temps = hourly.map { it.temperature }
    val minTemp = temps.min()
    val range = (temps.max() - minTemp).coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                hourly.take(12).forEach { h ->
                    Column(modifier = Modifier.width(52.dp).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${h.temperature.toInt()}°", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(h.time.format(hourFormatter), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                val stepX = size.width / (hourly.take(12).size - 1).coerceAtLeast(1)
                val path = Path()
                hourly.take(12).forEachIndexed { i, h ->
                    val x = i * stepX
                    val y = size.height - ((h.temperature - minTemp) / range * size.height).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = primaryColor, style = Stroke(width = 3.dp.toPx()))
                hourly.take(12).forEachIndexed { i, h ->
                    val x = i * stepX
                    val y = size.height - ((h.temperature - minTemp) / range * size.height).toFloat()
                    drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(x, y))
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(day: DailyForecast, dayFormatter: DateTimeFormatter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(day.date.format(dayFormatter).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(weatherSymbolLabel(day.dominantSymbol), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
            Text("${day.tempMin.toInt()}° / ${day.tempMax.toInt()}°", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
