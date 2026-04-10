package ru.towich.achline.presentation.topics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.towich.achline.domain.topics.TopicsTree

class TopicsViewModel(
    private val loadTopicsTree: suspend () -> TopicsTree,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopicsUiState())
    val uiState: StateFlow<TopicsUiState> = _uiState.asStateFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { loadTopicsTree() }
                .onSuccess { tree ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            tree = tree,
                            level = TopicsLevel.Technologies,
                            selectedTechnologyId = null,
                            selectedCategoryId = null,
                            selectedThemeId = null,
                            selectedQuestionIdForAnswer = null,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: e.toString()) }
                }
        }
    }

    fun onTechnologyClick(technologyId: String) {
        _uiState.update { reduceTopicsState(it, TopicsUiAction.TechnologyClicked(technologyId)) }
    }

    fun onCategoryClick(categoryId: String) {
        _uiState.update { reduceTopicsState(it, TopicsUiAction.CategoryClicked(categoryId)) }
    }

    fun onThemeClick(themeId: String) {
        _uiState.update { reduceTopicsState(it, TopicsUiAction.ThemeClicked(themeId)) }
    }

    fun onQuestionClick(questionId: String) {
        _uiState.update { reduceTopicsState(it, TopicsUiAction.QuestionClicked(questionId)) }
    }

    fun dismissAnswerDialog() {
        _uiState.update { reduceTopicsState(it, TopicsUiAction.DismissAnswerDialog) }
    }

    fun onBack(): Boolean {
        val (next, consumed) = backFromTopicsState(_uiState.value)
        _uiState.value = next
        return consumed
    }
}
