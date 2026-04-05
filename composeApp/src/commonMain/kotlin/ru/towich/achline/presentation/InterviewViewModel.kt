package ru.towich.achline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.towich.achline.data.InterviewRepository
import ru.towich.achline.domain.MergedQuestion
import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.mergeBundleWithOverlay
import ru.towich.achline.domain.newUserQuestionId
import ru.towich.achline.domain.pickStackQuestionIds
import ru.towich.achline.domain.progressFor
import ru.towich.achline.domain.selectNextQuestion
import ru.towich.achline.domain.withProgress

data class ThemeOption(
    val technologyId: String,
    val categoryId: String,
    val themeId: String,
    val themeTitle: String,
) {
    val label: String
        get() = "$technologyId · $categoryId · $themeTitle"
}

data class StackCardUi(
    val question: MergedQuestion,
    val correctCount: Int,
    val shownCount: Int,
    val answerVisible: Boolean,
)

data class InterviewUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val stack: List<StackCardUi> = emptyList(),
    val showAddDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val themeOptions: List<ThemeOption> = emptyList(),
)

class InterviewViewModel(
    private val repository: InterviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private var bundleThemes: List<ThemeBundleData> = emptyList()
    private var mergedPool: List<MergedQuestion> = emptyList()
    private var overlay: UserOverlayState = UserOverlayState()
    private var stackIds: List<String> = emptyList()
    private val answerVisibleIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: e.toString())
                    }
                }
        }
    }

    private suspend fun loadInternal() {
        val (themes, ovl) = repository.loadBundleAndOverlay()
        bundleThemes = themes
        overlay = ovl
        mergedPool = mergeBundleWithOverlay(bundleThemes, overlay)
        stackIds = pickStackQuestionIds(mergedPool, overlay.progress, minOf(3, mergedPool.size))
        stackIds.firstOrNull()?.let { top ->
            overlay = overlay.withProgress(top) { it.copy(shownCount = it.shownCount + 1) }
        }
        repository.saveOverlay(overlay)
        emitState()
    }

    fun toggleAnswer(questionId: String) {
        if (!answerVisibleIds.add(questionId)) {
            answerVisibleIds.remove(questionId)
        }
        emitState()
    }

    fun openAddDialog() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun dismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun openDeleteConfirm() {
        if (stackIds.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteConfirm = true) }
        }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun onSwipeRight() {
        val top = stackIds.firstOrNull() ?: return
        overlay = overlay.withProgress(top) { it.copy(correctCount = it.correctCount + 1) }
        answerVisibleIds.remove(top)
        advanceStackAfterSwipe()
    }

    fun onSwipeLeft() {
        val top = stackIds.firstOrNull() ?: return
        answerVisibleIds.remove(top)
        advanceStackAfterSwipe()
    }

    private fun advanceStackAfterSwipe() {
        stackIds = stackIds.drop(1)
        fillStackUpToThree()
        stackIds.firstOrNull()?.let { top ->
            overlay = overlay.withProgress(top) { it.copy(shownCount = it.shownCount + 1) }
        }
        repository.saveOverlay(overlay)
        emitState()
    }

    private fun fillStackUpToThree() {
        val target = minOf(3, mergedPool.size).coerceAtLeast(0)
        while (stackIds.size < target) {
            val next = selectNextQuestion(mergedPool, overlay.progress, stackIds.toSet()) ?: break
            stackIds = stackIds + next.id
        }
    }

    fun submitAddQuestion(option: ThemeOption, questionText: String, answerText: String) {
        val textQ = questionText.trim()
        val textA = answerText.trim()
        if (textQ.isEmpty() || textA.isEmpty()) return
        val mq = MergedQuestion(
            id = newUserQuestionId(),
            questionText = textQ,
            answerText = textA,
            technologyId = option.technologyId,
            categoryId = option.categoryId,
            themeId = option.themeId,
            themeTitle = option.themeTitle,
            fromBundle = false,
        )
        overlay = overlay.copy(addedQuestions = overlay.addedQuestions + mq)
        mergedPool = mergeBundleWithOverlay(bundleThemes, overlay)
        fillStackUpToThree()
        repository.saveOverlay(overlay)
        _uiState.update { it.copy(showAddDialog = false) }
        emitState()
    }

    fun confirmRemoveTopCard() {
        val id = stackIds.firstOrNull() ?: return
        val q = mergedPool.find { it.id == id } ?: return
        overlay = if (q.fromBundle) {
            overlay.copy(removedIds = overlay.removedIds + id)
        } else {
            overlay.copy(addedQuestions = overlay.addedQuestions.filter { it.id != id })
        }
        answerVisibleIds.remove(id)
        stackIds = stackIds.filter { it != id }
        mergedPool = mergeBundleWithOverlay(bundleThemes, overlay)
        fillStackUpToThree()
        stackIds.firstOrNull()?.let { top ->
            overlay = overlay.withProgress(top) { it.copy(shownCount = it.shownCount + 1) }
        }
        repository.saveOverlay(overlay)
        _uiState.update { it.copy(showDeleteConfirm = false) }
        emitState()
    }

    private fun emitState() {
        val options = bundleThemes
            .map { ThemeOption(it.technologyId, it.categoryId, it.themeId, it.themeTitle) }
            .distinct()
        _uiState.update { s ->
            s.copy(
                isLoading = false,
                error = null,
                stack = stackIds.mapNotNull { id ->
                    val q = mergedPool.find { it.id == id } ?: return@mapNotNull null
                    val p = overlay.progressFor(id)
                    StackCardUi(
                        question = q,
                        correctCount = p.correctCount,
                        shownCount = p.shownCount,
                        answerVisible = id in answerVisibleIds,
                    )
                },
                themeOptions = options,
            )
        }
    }
}
