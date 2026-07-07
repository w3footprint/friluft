package se.w3footprint.friluft.presentation.common.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    onPrimary = SurfaceLight,
    secondary = DeepBlue,
    onSecondary = SurfaceLight,
    tertiary = SunYellow,
    background = CloudGray,
    surface = SurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = NightBlue,
    secondary = SkyBlue,
    onSecondary = NightBlue,
    tertiary = SunYellow,
    background = NightBlue,
    surface = DeepBlue,
    onBackground = SurfaceLight,
    onSurface = SurfaceLight,
)

@Composable
fun FriLuftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FriLuftTypography,
        content = content,
    )
}
