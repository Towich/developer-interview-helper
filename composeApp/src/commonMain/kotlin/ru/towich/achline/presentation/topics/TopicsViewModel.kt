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
    private val loadTopicsTree: suspend (String) -> TopicsTree,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopicsUiState())
    val uiState: StateFlow<TopicsUiState> = _uiState.asStateFlow()

    fun reload() {
        val folder = _uiState.value.selectedFolder ?: return
        loadFolder(folder)
    }

    fun onFolderClick(folder: TopicsFolder) {
        _uiState.update {
            reduceTopicsState(it, TopicsUiAction.FolderClicked(folder)).copy(
                isLoading = true,
                error = null,
                tree = TopicsTree(emptyList()),
            )
        }
        loadFolder(folder)
    }

    private fun loadFolder(folder: TopicsFolder) {
        val resourceBasePath = when (folder) {
            TopicsFolder.Default -> "files/interview"
            TopicsFolder.Borisproit -> "files/borisproit"
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { loadTopicsTree(resourceBasePath) }
                .onSuccess { tree ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            tree = tree,
                            level = TopicsLevel.Technologies,
                            selectedFolder = folder,
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
