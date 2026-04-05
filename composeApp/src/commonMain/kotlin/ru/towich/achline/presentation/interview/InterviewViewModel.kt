package ru.towich.achline.presentation.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.towich.achline.domain.interview.InterviewSessionEvent
import ru.towich.achline.domain.interview.InterviewSessionState
import ru.towich.achline.domain.interview.reduceInterviewSession
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.progressFor
import ru.towich.achline.domain.repository.InterviewRepository

class InterviewViewModel(
    private val repository: InterviewRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private var session: InterviewSessionState = InterviewSessionState(
        bundleThemes = emptyList(),
        overlay = UserOverlayState(),
        stackIds = emptyList(),
    )
    private val answerVisibleIds = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            runCatching {
                val (themes, overlay) = repository.loadBundleAndOverlay()
                applySessionEvent(InterviewSessionEvent.Initialized(themes, overlay))
            }.onFailure { e ->
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: e.toString())
                }
            }
        }
    }

    fun dispatch(intent: InterviewIntent) {
        when (intent) {
            is InterviewIntent.ToggleAnswer -> {
                val id = intent.questionId
                if (!answerVisibleIds.add(id)) {
                    answerVisibleIds.remove(id)
                }
                emitUiFromSession()
            }

            InterviewIntent.SwipeLeft ->
                applySessionEvent(InterviewSessionEvent.SwipeLeft) {
                    session.stackIds.firstOrNull()?.let { answerVisibleIds.remove(it) }
                }

            InterviewIntent.SwipeRight ->
                applySessionEvent(InterviewSessionEvent.SwipeRight) {
                    session.stackIds.firstOrNull()?.let { answerVisibleIds.remove(it) }
                }
        }
    }

    private inline fun applySessionEvent(
        event: InterviewSessionEvent,
        beforeApply: () -> Unit = {},
    ) {
        beforeApply()
        val reduction = reduceInterviewSession(session, event)
        session = reduction.state
        if (reduction.persistOverlay) {
            repository.saveOverlay(session.overlay)
        }
        emitUiFromSession()
    }

    private fun emitUiFromSession() {
        val merged = session.mergedPool()
        _uiState.update { s ->
            s.copy(
                isLoading = false,
                error = null,
                stack = session.stackIds.mapNotNull { id ->
                    val q = merged.find { it.id == id } ?: return@mapNotNull null
                    val p = session.overlay.progressFor(id)
                    StackCardUi(
                        question = q,
                        correctCount = p.correctCount,
                        shownCount = p.shownCount,
                        answerVisible = id in answerVisibleIds,
                    )
                },
            )
        }
    }
}
