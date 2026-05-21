package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ProfessionalAccent,
    secondary = IndustrialYellow,
    tertiary = ColorPrDone,
    background = CharcoalDark,
    surface = CharcoalLight,
    onPrimary = ProfessionalOnAccent,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ProfessionalAccent,
    secondary = IndustrialYellow,
    tertiary = ColorPrDone,
    background = CharcoalDark, // Standardize to Professional Dark UI across themes
    surface = CharcoalLight,
    onPrimary = ProfessionalOnAccent,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE2E2E6),
    onSurface = Color(0xFFE2E2E6)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for premium professional dark UI
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false, // Set to false to preserve exact professional design theme branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme // Force use of our cohesive, premium Dark theme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
