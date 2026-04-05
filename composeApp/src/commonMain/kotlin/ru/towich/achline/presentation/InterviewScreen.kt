package ru.towich.achline.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun InterviewScreen(
    modifier: Modifier = Modifier,
) {
    val repository = LocalInterviewRepository.current
    val vm: InterviewViewModel = viewModel { InterviewViewModel(repository) }
    val state by vm.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                if (state.stack.isEmpty()) {
                    Text(
                        text = "Нет доступных вопросов",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    CardStack(
                        stack = state.stack,
                        onSwipeLeft = vm::onSwipeLeft,
                        onSwipeRight = vm::onSwipeRight,
                        onToggleAnswer = vm::toggleAnswer,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 88.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { vm.openDeleteConfirm() },
                modifier = Modifier.size(56.dp),
            ) {
                Text("✕", style = MaterialTheme.typography.titleLarge)
            }
            FloatingActionButton(
                onClick = { vm.openAddDialog() },
                modifier = Modifier.size(56.dp),
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteConfirm() },
            title = { Text("Удалить карточку?") },
            text = { Text("Текущая верхняя карточка будет скрыта из тренировки (для вопросов из бандла — мягкое удаление).") },
            confirmButton = {
                TextButton(onClick = { vm.confirmRemoveTopCard() }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissDeleteConfirm() }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (state.showAddDialog) {
        AddQuestionDialog(
            themeOptions = state.themeOptions,
            onDismiss = { vm.dismissAddDialog() },
            onSubmit = { option, q, a -> vm.submitAddQuestion(option, q, a) },
        )
    }
}

@Composable
private fun CardStack(
    stack: List<StackCardUi>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val w = constraints.maxWidth.toFloat()
        val top = stack.firstOrNull() ?: return@BoxWithConstraints
        val below = stack.drop(1).asReversed()

        below.forEachIndexed { index, card ->
            key(card.question.id) {
                val layerFromBottom = below.size - index
                val scale = 1f - 0.05f * layerFromBottom
                val yOffset = (layerFromBottom * 10).dp
                QuestionCard(
                    card = card,
                    onToggleAnswer = { onToggleAnswer(card.question.id) },
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = yOffset)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = 0.92f
                        },
                    interactive = false,
                )
            }
        }

        SwipeableTopCard(
            card = top,
            widthPx = w,
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            onToggleAnswer = { onToggleAnswer(top.question.id) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun SwipeableTopCard(
    card: StackCardUi,
    widthPx: Float,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onToggleAnswer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = widthPx * 0.22f

    LaunchedEffect(card.question.id) {
        offsetX.snapTo(0f)
    }

    QuestionCard(
        card = card,
        onToggleAnswer = onToggleAnswer,
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .pointerInput(card.question.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > threshold -> {
                                    offsetX.animateTo(widthPx * 1.5f, tween(180))
                                    onSwipeRight()
                                }

                                offsetX.value < -threshold -> {
                                    offsetX.animateTo(-widthPx * 1.5f, tween(180))
                                    onSwipeLeft()
                                }

                                else -> offsetX.animateTo(0f, tween(160))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f, tween(120)) }
                    },
                ) { _, dragAmount ->
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount.x)
                    }
                }
            },
        interactive = true,
    )
}

@Composable
private fun QuestionCard(
    card: StackCardUi,
    onToggleAnswer: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = if (interactive) 6.dp else 2.dp,
        shadowElevation = if (interactive) 8.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "${card.question.technologyId} · ${card.question.themeTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = card.question.questionText,
                style = MaterialTheme.typography.titleMedium,
            )
            Button(onClick = onToggleAnswer, enabled = interactive) {
                Text(if (card.answerVisible) "Скрыть ответ" else "Показать ответ")
            }
            if (card.answerVisible) {
                Text(
                    text = card.question.answerText,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Успех: ${card.correctCount} · Показов: ${card.shownCount}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddQuestionDialog(
    themeOptions: List<ThemeOption>,
    onDismiss: () -> Unit,
    onSubmit: (ThemeOption, String, String) -> Unit,
) {
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }
    var selected by remember(themeOptions) { mutableStateOf(themeOptions.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый вопрос") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (themeOptions.isEmpty()) {
                    Text("Нет тем из бандла — добавьте JSON-файлы тем в ресурсы.")
                } else {
                    Text("Тема", style = MaterialTheme.typography.labelLarge)
                    themeOptions.forEach { option ->
                        TextButton(
                            onClick = { selected = option },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = option.label,
                                style = if (option == selected) {
                                    MaterialTheme.typography.bodyLarge
                                } else {
                                    MaterialTheme.typography.bodyMedium
                                },
                                color = if (option == selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Вопрос") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Ответ") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val opt = selected ?: return@TextButton
                    onSubmit(opt, questionText, answerText)
                    questionText = ""
                    answerText = ""
                },
                enabled = selected != null && questionText.isNotBlank() && answerText.isNotBlank(),
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
    )
}
