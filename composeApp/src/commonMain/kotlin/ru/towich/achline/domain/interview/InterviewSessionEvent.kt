package ru.towich.achline.domain.interview

import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState

sealed interface InterviewSessionEvent {
    data class Initialized(
        val bundleThemes: List<ThemeBundleData>,
        val overlay: UserOverlayState,
    ) : InterviewSessionEvent

    data object SwipeRight : InterviewSessionEvent
    data object SwipeLeft : InterviewSessionEvent

    data class AddUserQuestion(
        val theme: ThemeRef,
        val questionText: String,
        val answerText: String,
    ) : InterviewSessionEvent

    data object ConfirmRemoveTopCard : InterviewSessionEvent
}
