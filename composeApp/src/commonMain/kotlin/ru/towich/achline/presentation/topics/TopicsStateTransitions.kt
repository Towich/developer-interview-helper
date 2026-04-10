package ru.towich.achline.presentation.topics

sealed interface TopicsUiAction {
    data class TechnologyClicked(val technologyId: String) : TopicsUiAction
    data class CategoryClicked(val categoryId: String) : TopicsUiAction
    data class ThemeClicked(val themeId: String) : TopicsUiAction
    data class QuestionClicked(val questionId: String) : TopicsUiAction
    data object DismissAnswerDialog : TopicsUiAction
}

fun reduceTopicsState(
    state: TopicsUiState,
    action: TopicsUiAction,
): TopicsUiState = when (action) {
    is TopicsUiAction.TechnologyClicked -> state.copy(
        level = TopicsLevel.Categories,
        selectedTechnologyId = action.technologyId,
        selectedCategoryId = null,
        selectedThemeId = null,
        selectedQuestionIdForAnswer = null,
    )
    is TopicsUiAction.CategoryClicked -> state.copy(
        level = TopicsLevel.Themes,
        selectedCategoryId = action.categoryId,
        selectedThemeId = null,
        selectedQuestionIdForAnswer = null,
    )
    is TopicsUiAction.ThemeClicked -> state.copy(
        level = TopicsLevel.Questions,
        selectedThemeId = action.themeId,
        selectedQuestionIdForAnswer = null,
    )
    is TopicsUiAction.QuestionClicked -> state.copy(selectedQuestionIdForAnswer = action.questionId)
    TopicsUiAction.DismissAnswerDialog -> state.copy(selectedQuestionIdForAnswer = null)
}

fun backFromTopicsState(state: TopicsUiState): Pair<TopicsUiState, Boolean> {
    if (state.selectedQuestionIdForAnswer != null) {
        return state.copy(selectedQuestionIdForAnswer = null) to true
    }
    return when (state.level) {
        TopicsLevel.Technologies -> state to false
        TopicsLevel.Categories -> {
            state.copy(
                level = TopicsLevel.Technologies,
                selectedTechnologyId = null,
                selectedCategoryId = null,
                selectedThemeId = null,
            ) to true
        }
        TopicsLevel.Themes -> {
            state.copy(
                level = TopicsLevel.Categories,
                selectedCategoryId = null,
                selectedThemeId = null,
            ) to true
        }
        TopicsLevel.Questions -> {
            state.copy(
                level = TopicsLevel.Themes,
                selectedThemeId = null,
            ) to true
        }
    }
}
