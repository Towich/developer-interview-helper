package ru.towich.achline.presentation.interview

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlinx.coroutines.launch
import ru.towich.achline.presentation.LocalInterviewRepository

private val GradientPink = Color(0xFFFF4D8C)
private val GradientViolet = Color(0xFF9D4EDD)
private val GradientCyan = Color(0xFF5CE1E6)
private val CardInnerBg = Color(0xFF120A1F)
private val ChipBg = Color(0x33FFFFFF)

@Composable
fun InterviewScreen(
    modifier: Modifier = Modifier,
) {
    val repository = LocalInterviewRepository.current
    val vm: InterviewViewModel = viewModel { InterviewViewModel(repository) }
    val state by vm.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF0D0618),
                        Color(0xFF16082A),
                    ),
                ),
            ),
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
                        .padding(24.dp)
                        .windowInsetsPadding(WindowInsets.statusBars),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            else -> {
                if (state.stack.isEmpty()) {
                    Text(
                        text = "Нет доступных вопросов",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .windowInsetsPadding(WindowInsets.statusBars),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    CardStack(
                        stack = state.stack,
                        onSwipeLeft = { vm.dispatch(InterviewIntent.SwipeLeft) },
                        onSwipeRight = { vm.dispatch(InterviewIntent.SwipeRight) },
                        onToggleAnswer = { vm.dispatch(InterviewIntent.ToggleAnswer(it)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(bottom = 100.dp, top = 12.dp, start = 18.dp, end = 18.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { vm.dispatch(InterviewIntent.OpenDeleteConfirm) },
                modifier = Modifier.size(58.dp),
                containerColor = Color(0xFF2A1F3D),
                contentColor = GradientPink,
                elevation = FloatingActionButtonDefaults.loweredElevation(),
            ) {
                Text("✕", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            FloatingActionButton(
                onClick = { vm.dispatch(InterviewIntent.OpenAddDialog) },
                modifier = Modifier.size(58.dp),
                containerColor = Color(0xFF2A1F3D),
                contentColor = GradientCyan,
                elevation = FloatingActionButtonDefaults.loweredElevation(),
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { vm.dispatch(InterviewIntent.DismissDeleteConfirm) },
            title = { Text("Удалить карточку?") },
            text = { Text("Текущая верхняя карточка будет скрыта из тренировки (для вопросов из бандла — мягкое удаление).") },
            confirmButton = {
                TextButton(onClick = { vm.dispatch(InterviewIntent.ConfirmRemoveTopCard) }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dispatch(InterviewIntent.DismissDeleteConfirm) }) {
                    Text("Отмена")
                }
            },
        )
    }

    if (state.showAddDialog) {
        AddQuestionDialog(
            themeOptions = state.themeOptions,
            onDismiss = { vm.dispatch(InterviewIntent.DismissAddDialog) },
            onSubmit = { option, q, a ->
                vm.dispatch(InterviewIntent.SubmitAddQuestion(option, q, a))
            },
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
        // Рисуем ровно два слоя: верхняя + одна под ней (без третьей и без всей глубины стопки).
        val peekCard = stack.getOrNull(1)

        peekCard?.let { card ->
            key(card.question.id) {
                val scale = 1f - StackPeekScaleDelta
                val yOffset = StackPeekYOffsetDp.dp
                // Тот же макет, что у верхней (CardFrontFace), иначе при «промоушене» текст дёргается.
                FlipInterviewCard(
                    card = card,
                    onToggleAnswer = {},
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = yOffset)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = StackPeekAlpha
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

private const val StackPeekScaleDelta = 0.05f
private const val StackPeekYOffsetDp = 10
private const val StackPeekAlpha = 0.88f

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
    // Отдельный Animatable на каждую карточку: иначе после свайпа ухода offsetX остаётся
    // за пределами экрана до первого кадра LaunchedEffect — один кадр «моргания».
    val offsetX = remember(card.question.id) { Animatable(0f) }
    val density = LocalDensity.current

    var firstStackTopEver by remember { mutableStateOf(true) }
    val promoteProgress = remember(card.question.id) {
        val start = if (firstStackTopEver) {
            firstStackTopEver = false
            1f
        } else {
            0f
        }
        Animatable(start)
    }

    val threshold = widthPx * 0.22f

    LaunchedEffect(card.question.id) {
        if (promoteProgress.value < 1f) {
            promoteProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            )
        }
    }

    val p = promoteProgress.value
    val promoteScale = 1f - StackPeekScaleDelta * (1f - p)
    // Верхняя карточка всегда непрозрачная: иначе сквозь неё видна новая peek (следующая в стеке) —
    // на долю секунды «вспыхивает» текст третьей карточки.
    val promoteOffsetY = with(density) { StackPeekYOffsetDp.dp.toPx() } * (1f - p)

    // Tinder-style: наклон пропорционален смещению по X (см. типичные формулы offset/width * maxAngle).
    val maxTiltDeg = 18f
    val tiltFactor = 2.2f
    val swipeRotationZ =
        (offsetX.value / widthPx * maxTiltDeg * tiltFactor).coerceIn(-maxTiltDeg, maxTiltDeg)
    val swipeLiftY = -abs(offsetX.value) / 18f

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = swipeLiftY
                rotationZ = swipeRotationZ
                transformOrigin = TransformOrigin(0.5f, 0.92f)
            }
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = promoteScale
                    scaleY = promoteScale
                    alpha = 1f
                    translationY = promoteOffsetY
                },
        ) {
            FlipInterviewCard(
                card = card,
                onToggleAnswer = onToggleAnswer,
                modifier = Modifier.fillMaxSize(),
                interactive = true,
            )
        }
    }
}

@Composable
private fun FlipInterviewCard(
    card: StackCardUi,
    onToggleAnswer: () -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean,
) {
    if (!interactive) {
        CardFrontFace(
            card = card,
            onShowAnswer = onToggleAnswer,
            interactive = false,
            modifier = modifier,
        )
        return
    }

    val targetRotation = if (card.answerVisible) 180f else 0f
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "flip",
    )
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density.density
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (rotation <= 90f) 1f else 0f),
        ) {
            CardFrontFace(
                card = card,
                onShowAnswer = onToggleAnswer,
                interactive = interactive,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (rotation > 90f) 1f else 0f)
                .graphicsLayer { rotationY = 180f },
        ) {
            CardBackFace(
                card = card,
                onFlipToQuestion = onToggleAnswer,
                interactive = interactive && card.answerVisible,
            )
        }
    }
}

@Composable
private fun CardFrontFace(
    card: StackCardUi,
    onShowAnswer: () -> Unit,
    interactive: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(listOf(GradientPink, GradientViolet, GradientCyan)),
            )
            .padding(2.dp)
            .background(CardInnerBg, RoundedCornerShape(26.dp)),
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp)) {
            ThemeChip(
                text = "${card.question.technologyId} · ${card.question.themeTitle}",
                modifier = Modifier.align(Alignment.TopStart),
            )
            Text(
                text = card.question.questionText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GenZGradientButton(
                    text = "Показать ответ",
                    onClick = onShowAnswer,
                    enabled = interactive,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Успех: ${card.correctCount} · Показов: ${card.shownCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CardBackFace(
    card: StackCardUi,
    onFlipToQuestion: () -> Unit,
    interactive: Boolean,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(GradientViolet, Color(0xFF6B2D8C), GradientPink),
                ),
            )
            .padding(2.dp)
            .background(Color(0xFF0F0818), RoundedCornerShape(26.dp))
            .clickable(
                enabled = interactive,
                interactionSource = interaction,
                indication = null,
                onClick = onFlipToQuestion,
            ),
    ) {
        Text(
            text = "ОТВЕТ",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = GradientCyan,
            letterSpacing = 3.sp,
        )
        Text(
            text = card.question.answerText,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 22.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (interactive) {
            Text(
                text = "тапни, чтобы вернуться к вопросу",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp, start = 16.dp, end = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThemeChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ChipBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = GradientPink,
            maxLines = 2,
        )
    }
}

@Composable
private fun GenZGradientButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // Без Material Button: при enabled=false он всё равно может давать мигание альфы/LocalContentColor.
    Box(
        modifier = modifier
            .height(52.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientPink, GradientViolet),
                ),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
        )
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
