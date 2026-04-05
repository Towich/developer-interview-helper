package ru.towich.achline.presentation.interview

import ru.towich.achline.domain.MergedQuestion

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
)
