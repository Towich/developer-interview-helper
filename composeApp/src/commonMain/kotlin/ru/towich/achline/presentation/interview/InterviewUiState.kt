package ru.towich.achline.presentation.interview

import ru.towich.achline.domain.MergedQuestion

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
