package ru.towich.achline.presentation.interview

sealed interface InterviewIntent {
    data class ToggleAnswer(val questionId: String) : InterviewIntent
    data object SwipeLeft : InterviewIntent
    data object SwipeRight : InterviewIntent
}
