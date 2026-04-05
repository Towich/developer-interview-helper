package ru.towich.achline

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ru.towich.achline.presentation.interview.InterviewScreen

private val AchlineDarkColors = darkColorScheme(
    primary = Color(0xFFFF4D8C),
    onPrimary = Color(0xFF1A0510),
    primaryContainer = Color(0xFF5A1E40),
    onPrimaryContainer = Color(0xFFFFD6E8),
    secondary = Color(0xFFB388FF),
    onSecondary = Color(0xFF1A0A2E),
    tertiary = Color(0xFF5CE1E6),
    background = Color(0xFF08050F),
    surface = Color(0xFF120C1A),
    surfaceVariant = Color(0xFF1E1630),
    onSurface = Color(0xFFF5F0FF),
    onSurfaceVariant = Color(0xFFC4BBD8),
    outline = Color(0xFF4A3F66),
)

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AchlineDarkColors) {
        Surface(modifier = Modifier.fillMaxSize(), color = AchlineDarkColors.background) {
            InterviewScreen(modifier = Modifier.fillMaxSize())
        }
    }
}
