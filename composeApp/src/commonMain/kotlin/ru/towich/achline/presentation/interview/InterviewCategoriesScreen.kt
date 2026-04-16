package ru.towich.achline.presentation.interview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.towich.achline.domain.InterviewStackMode
import ru.towich.achline.domain.hasOnlyCorrectAnswers
import ru.towich.achline.domain.mergeBundleWithOverlay
import ru.towich.achline.domain.progressFor
import ru.towich.achline.domain.repository.InterviewRepository
import ru.towich.achline.navigation.InterviewContentSource
import ru.towich.achline.presentation.LocalInterviewRepository

private val GradientPink = Color(0xFFFF4D8C)
private val AppVioletBg = Color(0xFF16082A)

private data class CategoryCounts(val all: Int, val leastAnswered: Int)

@Composable
fun InterviewCategoriesScreen(
    source: InterviewContentSource = InterviewContentSource.Default,
    repository: InterviewRepository = LocalInterviewRepository.current,
    onCategoryClick: (InterviewStackMode, String) -> Unit,
    resourceBasePath: String = "files/interview",
    modifier: Modifier = Modifier,
) {
    var counts by remember { mutableStateOf<CategoryCounts?>(null) }
    var borisproitCounts by remember { mutableStateOf<Map<String, CategoryCounts>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository, source, resourceBasePath) {
        runCatching {
            if (source == InterviewContentSource.Borisproit) {
                val modePaths = listOf(
                    "files/borisproit/android" to "Android",
                    "files/borisproit/kotlin" to "Kotlin",
                )
                val loaded = modePaths.associate { (path, _) ->
                    val (themes, overlay) = repository.loadBundleAndOverlay(path)
                    val merged = mergeBundleWithOverlay(themes, overlay)
                    val leastAnswered = merged.count { q -> !overlay.progressFor(q.id).hasOnlyCorrectAnswers() }
                    path to CategoryCounts(all = merged.size, leastAnswered = leastAnswered)
                }
                borisproitCounts = loaded
            } else {
                val (themes, overlay) = repository.loadBundleAndOverlay(resourceBasePath)
                val merged = mergeBundleWithOverlay(themes, overlay)
                val leastAnswered = merged.count { q -> !overlay.progressFor(q.id).hasOnlyCorrectAnswers() }
                counts = CategoryCounts(all = merged.size, leastAnswered = leastAnswered)
            }
        }.onFailure { e ->
            loadError = e.message ?: e.toString()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppVioletBg),
    ) {
        when {
            loadError != null -> {
                Text(
                    text = loadError ?: "",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            (source == InterviewContentSource.Borisproit && borisproitCounts == null) ||
                (source != InterviewContentSource.Borisproit && counts == null) -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GradientPink,
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = if (source == InterviewContentSource.Borisproit) "Borisproit" else "Собеседование",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Выберите режим",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (source == InterviewContentSource.Borisproit) {
                        val modeCards = listOf(
                            Triple("Android", "Режим по вопросам Android из базы Borisproit", "files/borisproit/android"),
                            Triple("Kotlin", "Режим по вопросам Kotlin из базы Borisproit", "files/borisproit/kotlin"),
                        )
                        val loadedCounts = borisproitCounts.orEmpty()
                        modeCards.forEach { (title, subtitle, path) ->
                            val modeCount = loadedCounts[path]?.all ?: 0
                            CategoryCard(
                                title = title,
                                subtitle = subtitle,
                                questionCount = modeCount,
                                onClick = { onCategoryClick(InterviewStackMode.AllQuestions, path) },
                            )
                        }
                    } else {
                        val c = counts!!
                        CategoryCard(
                            title = "Все вопросы",
                            subtitle = "Как в основном режиме: баланс по темам",
                            questionCount = c.all,
                            onClick = { onCategoryClick(InterviewStackMode.AllQuestions, resourceBasePath) },
                        )
                        CategoryCard(
                            title = "Реже отвечал",
                            subtitle = "Без вопросов с 100% успехов по показам; сначала с меньшим числом успехов",
                            questionCount = c.leastAnswered,
                            onClick = { onCategoryClick(InterviewStackMode.LeastAnswered, resourceBasePath) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewFoldersScreen(
    onFolderClick: (InterviewContentSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppVioletBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Категории",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Выберите папку",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CategoryCard(
                title = "Основное",
                subtitle = "Базовые режимы собеседования",
                questionCount = 0,
                onClick = { onFolderClick(InterviewContentSource.Default) },
            )
            CategoryCard(
                title = "Borisproit",
                subtitle = "Режимы из PDF-коллекции вопросов",
                questionCount = 0,
                onClick = { onFolderClick(InterviewContentSource.Borisproit) },
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    questionCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (questionCount > 0) {
                Text(
                    text = "Вопросов: $questionCount",
                    style = MaterialTheme.typography.labelLarge,
                    color = GradientPink,
                )
            }
        }
    }
}
