package ru.towich.achline.presentation.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.towich.achline.domain.repository.InterviewRepository
import ru.towich.achline.domain.topics.TopicsStats
import ru.towich.achline.domain.topics.TopicsQuestionItem
import ru.towich.achline.domain.topics.GetTopicsTreeUseCase
import ru.towich.achline.presentation.LocalInterviewRepository

private val GradientPink = Color(0xFFFF4D8C)
private val AppVioletBg = Color(0xFF16082A)

@Composable
fun TopicsScreen(
    modifier: Modifier = Modifier,
) {
    val repository = LocalInterviewRepository.current
    val vm: TopicsViewModel = viewModel {
        val useCase = createGetTopicsTreeUseCase(repository)
        TopicsViewModel(loadTopicsTree = { basePath -> useCase(basePath) })
    }
    val state by vm.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppVioletBg),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = GradientPink,
                )
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = vm::reload) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                TopicsContent(
                    state = state,
                    onBack = vm::onBack,
                    onFolderClick = vm::onFolderClick,
                    onTechnologyClick = vm::onTechnologyClick,
                    onCategoryClick = vm::onCategoryClick,
                    onThemeClick = vm::onThemeClick,
                    onQuestionClick = vm::onQuestionClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    val selectedQuestion = state.selectedQuestion
    if (selectedQuestion != null) {
        AlertDialog(
            onDismissRequest = vm::dismissAnswerDialog,
            title = { Text(selectedQuestion.question.questionText) },
            text = { Text(selectedQuestion.question.answerText) },
            confirmButton = {
                TextButton(onClick = vm::dismissAnswerDialog) {
                    Text("Закрыть")
                }
            },
        )
    }
}

@Composable
private fun TopicsContent(
    state: TopicsUiState,
    onBack: () -> Boolean,
    onFolderClick: (TopicsFolder) -> Unit,
    onTechnologyClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onThemeClick: (String) -> Unit,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (state.level) {
        TopicsLevel.Folders -> "Папки"
        TopicsLevel.Technologies -> "Технологии"
        TopicsLevel.Categories -> "Категории"
        TopicsLevel.Themes -> "Темы"
        TopicsLevel.Questions -> "Вопросы"
    }

    Column(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.level != TopicsLevel.Folders) {
            Text(
                text = "← Назад",
                modifier = Modifier.clickable { onBack() },
                color = GradientPink,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        when (state.level) {
            TopicsLevel.Folders -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.folders, key = { it.name }) { folder ->
                        val title = if (folder == TopicsFolder.Borisproit) "Borisproit" else "Основное"
                        val subtitle = if (folder == TopicsFolder.Borisproit) {
                            "Вопросы из Borisproit"
                        } else {
                            "Базовая база вопросов"
                        }
                        FolderEntryCard(
                            title = title,
                            subtitle = subtitle,
                            onClick = { onFolderClick(folder) },
                        )
                    }
                }
            }
            TopicsLevel.Technologies -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.technologies, key = { it.id }) { technology ->
                        TopicsEntryCard(
                            title = technology.id,
                            stats = technology.stats,
                            onClick = { onTechnologyClick(technology.id) },
                        )
                    }
                }
            }
            TopicsLevel.Categories -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.categories, key = { it.id }) { category ->
                        TopicsEntryCard(
                            title = category.id,
                            stats = category.stats,
                            onClick = { onCategoryClick(category.id) },
                        )
                    }
                }
            }
            TopicsLevel.Themes -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.themes, key = { it.id }) { theme ->
                        TopicsEntryCard(
                            title = theme.title.ifBlank { theme.id },
                            subtitle = theme.id,
                            stats = theme.stats,
                            onClick = { onThemeClick(theme.id) },
                        )
                    }
                }
            }
            TopicsLevel.Questions -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.questions, key = { it.question.id }) { item ->
                        QuestionEntryCard(item = item, onClick = { onQuestionClick(item.question.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicsEntryCard(
    title: String,
    stats: TopicsStats?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (stats != null) {
                StatsLine(stats = stats)
            }
        }
    }
}

@Composable
private fun FolderEntryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    TopicsEntryCard(
        title = title,
        subtitle = subtitle,
        stats = null,
        onClick = onClick,
    )
}

@Composable
private fun QuestionEntryCard(
    item: TopicsQuestionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "ID: ${item.question.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatsLine(stats = item.stats)
        }
    }
}

@Composable
private fun StatsLine(stats: TopicsStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Показов: ${stats.shownCount}",
            style = MaterialTheme.typography.labelLarge,
            color = GradientPink,
        )
        Text(
            text = "Правильных: ${stats.correctCount}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun createGetTopicsTreeUseCase(repository: InterviewRepository): GetTopicsTreeUseCase =
    GetTopicsTreeUseCase(repository)
