package ru.towich.achline.domain.interview

import ru.towich.achline.domain.InterviewStackMode
import ru.towich.achline.domain.MergedQuestion
import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.mergeBundleWithOverlay

data class InterviewSessionState(
    val bundleThemes: List<ThemeBundleData>,
    val overlay: UserOverlayState,
    val stackIds: List<String>,
    val stackMode: InterviewStackMode = InterviewStackMode.AllQuestions,
) {
    fun mergedPool(): List<MergedQuestion> = mergeBundleWithOverlay(bundleThemes, overlay)
}
