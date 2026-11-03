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

private val ChatFlowDarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = CyanOnPrimary,
    primaryContainer = CyanContainer,
    onPrimaryContainer = CyanOnContainer,
    secondary = IndigoSecondary,
    onSecondary = Color(0xFF1E1B4B),
    secondaryContainer = IndigoContainer,
    onSecondaryContainer = IndigoOnContainer,
    tertiary = EmeraldTertiary,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = EmeraldContainer,
    background = ObsidianBackground,
    onBackground = SlateTextPrimary,
    surface = SlateSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateOutline
)

private val ChatFlowLightColorScheme = lightColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF2F9),
    onPrimaryContainer = Color(0xFF00363D),
    secondary = Color(0xFF4F46E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun ChatFlowTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ChatFlowDarkColorScheme else ChatFlowLightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Backward-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = ChatFlowTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
