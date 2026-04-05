package ru.towich.achline.presentation.interview

import ru.towich.achline.domain.QuestionDifficulty

sealed interface InterviewIntent {
    data object OpenAddDialog : InterviewIntent
    data object DismissAddDialog : InterviewIntent
    data object OpenDeleteConfirm : InterviewIntent
    data object DismissDeleteConfirm : InterviewIntent

    data class ToggleAnswer(val questionId: String) : InterviewIntent
    data object SwipeLeft : InterviewIntent
    data object SwipeRight : InterviewIntent

    data class SubmitAddQuestion(
        val option: ThemeOption,
        val questionText: String,
        val answerText: String,
        val difficulty: QuestionDifficulty,
    ) : InterviewIntent

    data object ConfirmRemoveTopCard : InterviewIntent
}
