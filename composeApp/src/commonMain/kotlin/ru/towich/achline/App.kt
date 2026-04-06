package ru.towich.achline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.towich.achline.navigation.InterviewCategoriesRoute
import ru.towich.achline.navigation.InterviewSessionRoute
import ru.towich.achline.navigation.InterviewSessionRouteTypeMap
import ru.towich.achline.navigation.TopicsRoute
import ru.towich.achline.presentation.interview.InterviewCategoriesScreen
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
private fun TopicsPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Каталог тем — скоро",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AchlineDarkColors) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val interviewTabSelected =
            currentDestination?.hasRoute(InterviewCategoriesRoute::class) == true ||
                currentDestination?.hasRoute(InterviewSessionRoute::class) == true

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AchlineDarkColors.background,
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Text(text = "В", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Собеседование") },
                        selected = interviewTabSelected,
                        onClick = {
                            navController.navigate(InterviewCategoriesRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                    NavigationBarItem(
                        icon = { Text(text = "Т", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Темы") },
                        selected = currentDestination?.hasRoute(TopicsRoute::class) == true,
                        onClick = {
                            navController.navigate(TopicsRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = InterviewCategoriesRoute,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                typeMap = InterviewSessionRouteTypeMap,
            ) {
                composable<InterviewCategoriesRoute> {
                    InterviewCategoriesScreen(
                        onCategoryClick = { mode ->
                            navController.navigate(InterviewSessionRoute(mode))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<InterviewSessionRoute>(typeMap = InterviewSessionRouteTypeMap) { entry ->
                    val route = entry.toRoute<InterviewSessionRoute>()
                    InterviewScreen(
                        mode = route.mode,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<TopicsRoute> {
                    TopicsPlaceholderScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
