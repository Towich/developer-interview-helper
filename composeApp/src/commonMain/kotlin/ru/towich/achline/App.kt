package ru.towich.achline

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import ru.towich.achline.domain.InterviewStackMode
import ru.towich.achline.navigation.InterviewCategoriesRoute
import ru.towich.achline.navigation.InterviewContentSource
import ru.towich.achline.navigation.InterviewModesRoute
import ru.towich.achline.navigation.InterviewSessionRoute
import ru.towich.achline.navigation.InterviewSessionRouteTypeMap
import ru.towich.achline.navigation.TopicsRoute
import ru.towich.achline.presentation.LocalInterviewRepository
import ru.towich.achline.presentation.interview.InterviewCategoriesScreen
import ru.towich.achline.presentation.interview.InterviewFoldersScreen
import ru.towich.achline.presentation.interview.InterviewScreen
import ru.towich.achline.presentation.topics.TopicsScreen

private val AchlineDarkColors = darkColorScheme(
    primary = Color(0xFFFF4D8C),
    onPrimary = Color(0xFF1A0510),
    primaryContainer = Color(0xFF5A1E40),
    onPrimaryContainer = Color(0xFFFFD6E8),
    secondary = Color(0xFFB388FF),
    onSecondary = Color(0xFF1A0A2E),
    tertiary = Color(0xFF5CE1E6),
    background = Color(0xFF16082A),
    surface = Color(0xFF120C1A),
    surfaceVariant = Color(0xFF1E1630),
    onSurface = Color(0xFFF5F0FF),
    onSurfaceVariant = Color(0xFFC4BBD8),
    outline = Color(0xFF4A3F66),
)

private data class BottomTabItem(
    val label: String,
    val iconText: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
@Preview
fun App() {
    MaterialTheme(colorScheme = AchlineDarkColors) {
        val navController = rememberNavController()
        val baseRepository = LocalInterviewRepository.current
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val categoriesTabSelected =
            currentDestination?.hasRoute(InterviewCategoriesRoute::class) == true ||
                currentDestination?.hasRoute(InterviewModesRoute::class) == true
        val interviewTabSelected = currentDestination?.hasRoute(InterviewSessionRoute::class) == true
        val topicsTabSelected = currentDestination?.hasRoute(TopicsRoute::class) == true
        val navUnderlayColor = AchlineDarkColors.background

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AchlineDarkColors.background,
            bottomBar = {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AchlineDarkColors.onPrimaryContainer,
                    selectedTextColor = AchlineDarkColors.onPrimaryContainer,
                    unselectedIconColor = AchlineDarkColors.onSurfaceVariant,
                    unselectedTextColor = AchlineDarkColors.onSurfaceVariant,
                    indicatorColor = AchlineDarkColors.primary.copy(alpha = 0.35f),
                )
                val tabs = listOf(
                    BottomTabItem(
                        label = "Категории",
                        iconText = "\uD83D\uDCC2",
                        selected = categoriesTabSelected,
                        onClick = {
                            val returnedToCategories = navController.popBackStack(
                                route = InterviewCategoriesRoute,
                                inclusive = false,
                                saveState = false,
                            )
                            if (!returnedToCategories) {
                                navController.navigate(InterviewCategoriesRoute) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    ),
                    BottomTabItem(
                        label = "Собес",
                        iconText = "\uD83D\uDCAC",
                        selected = interviewTabSelected,
                        onClick = {
                            navController.navigate(
                                InterviewSessionRoute(
                                    source = InterviewContentSource.Default,
                                    mode = InterviewStackMode.AllQuestions,
                                    resourceBasePath = "files/interview",
                                ),
                            ) {
                                popUpTo(InterviewCategoriesRoute) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    ),
                    BottomTabItem(
                        label = "Темы",
                        iconText = "\u2728",
                        selected = topicsTabSelected,
                        onClick = {
                            navController.navigate(TopicsRoute) {
                                popUpTo(InterviewCategoriesRoute) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    ),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(navUnderlayColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AchlineDarkColors.surfaceVariant.copy(alpha = 0.88f),
                                shape = RoundedCornerShape(28.dp),
                            )
                            .border(
                                width = 1.dp,
                                color = AchlineDarkColors.outline.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(28.dp),
                            ),
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                        ) {
                            tabs.forEach { item ->
                                val scale by animateFloatAsState(
                                    targetValue = if (item.selected) 1.08f else 1f,
                                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                )
                                val iconColor by animateColorAsState(
                                    targetValue = if (item.selected) AchlineDarkColors.onPrimaryContainer else AchlineDarkColors.onSurfaceVariant,
                                    animationSpec = tween(durationMillis = 220),
                                )

                                NavigationBarItem(
                                    icon = {
                                        Text(
                                            text = item.iconText,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = iconColor,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            },
                                        )
                                    },
                                    label = { Text(item.label) },
                                    selected = item.selected,
                                    onClick = item.onClick,
                                    colors = navItemColors,
                                )
                            }
                        }
                    }
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
                    InterviewFoldersScreen(
                        onFolderClick = { source ->
                            navController.navigate(InterviewModesRoute(source))
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<InterviewModesRoute> { entry ->
                    val route = entry.toRoute<InterviewModesRoute>()
                    val resourceBasePath = when (route.source) {
                        InterviewContentSource.Default -> "files/interview"
                        InterviewContentSource.Borisproit -> "files/borisproit"
                    }
                    InterviewCategoriesScreen(
                        source = route.source,
                        repository = baseRepository,
                        resourceBasePath = resourceBasePath,
                        onCategoryClick = { mode, selectedResourceBasePath ->
                            navController.navigate(
                                InterviewSessionRoute(
                                    source = route.source,
                                    mode = mode,
                                    resourceBasePath = selectedResourceBasePath,
                                ),
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<InterviewSessionRoute>(typeMap = InterviewSessionRouteTypeMap) { entry ->
                    val route = entry.toRoute<InterviewSessionRoute>()
                    val resourceBasePath = route.resourceBasePath ?: when (route.source) {
                        InterviewContentSource.Default -> "files/interview"
                        InterviewContentSource.Borisproit -> "files/borisproit/android"
                    }
                    InterviewScreen(
                        repository = baseRepository,
                        resourceBasePath = resourceBasePath,
                        mode = route.mode,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                composable<TopicsRoute> {
                    TopicsScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
