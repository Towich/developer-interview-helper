package ru.towich.achline.domain.interview

import ru.towich.achline.domain.MergedQuestion
import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.mergeBundleWithOverlay
import ru.towich.achline.domain.newUserQuestionId
import ru.towich.achline.domain.pickStackQuestionIds
import ru.towich.achline.domain.selectNextQuestion
import ru.towich.achline.domain.withProgress

data class InterviewSessionReduction(
    val state: InterviewSessionState,
    val persistOverlay: Boolean,
)

private fun fillStackIds(
    bundleThemes: List<ThemeBundleData>,
    overlay: UserOverlayState,
    stackIds: List<String>,
): List<String> {
    val merged = mergeBundleWithOverlay(bundleThemes, overlay)
    val target = minOf(3, merged.size).coerceAtLeast(0)
    val result = stackIds.toMutableList()
    while (result.size < target) {
        val next = selectNextQuestion(merged, overlay.progress, result.toSet()) ?: break
        result += next.id
    }
    return result
}

private fun bumpShownForTopIfAny(
    bundleThemes: List<ThemeBundleData>,
    overlay: UserOverlayState,
    stackIds: List<String>,
): Pair<UserOverlayState, Boolean> {
    val top = stackIds.firstOrNull() ?: return overlay to false
    val next = overlay.withProgress(top) { it.copy(shownCount = it.shownCount + 1) }
    return next to true
}

fun reduceInterviewSession(
    state: InterviewSessionState,
    event: InterviewSessionEvent,
): InterviewSessionReduction {
    return when (event) {
        is InterviewSessionEvent.Initialized -> {
            val merged = mergeBundleWithOverlay(event.bundleThemes, event.overlay)
            val depth = minOf(3, merged.size)
            var stackIds = pickStackQuestionIds(merged, event.overlay.progress, depth)
            val (overlay, changed) = bumpShownForTopIfAny(event.bundleThemes, event.overlay, stackIds)
            InterviewSessionReduction(
                state = InterviewSessionState(
                    bundleThemes = event.bundleThemes,
                    overlay = overlay,
                    stackIds = stackIds,
                ),
                persistOverlay = changed,
            )
        }

        InterviewSessionEvent.SwipeRight -> {
            val top = state.stackIds.firstOrNull()
                ?: return InterviewSessionReduction(state, persistOverlay = false)
            var overlay = state.overlay.withProgress(top) { it.copy(correctCount = it.correctCount + 1) }
            var stackIds = state.stackIds.drop(1)
            stackIds = fillStackIds(state.bundleThemes, overlay, stackIds)
            val bump = bumpShownForTopIfAny(state.bundleThemes, overlay, stackIds)
            overlay = bump.first
            InterviewSessionReduction(
                state = state.copy(overlay = overlay, stackIds = stackIds),
                persistOverlay = true,
            )
        }

        InterviewSessionEvent.SwipeLeft -> {
            if (state.stackIds.isEmpty()) {
                return InterviewSessionReduction(state, persistOverlay = false)
            }
            var overlay = state.overlay
            var stackIds = state.stackIds.drop(1)
            stackIds = fillStackIds(state.bundleThemes, overlay, stackIds)
            val bump = bumpShownForTopIfAny(state.bundleThemes, overlay, stackIds)
            overlay = bump.first
            InterviewSessionReduction(
                state = state.copy(overlay = overlay, stackIds = stackIds),
                persistOverlay = true,
            )
        }

        is InterviewSessionEvent.AddUserQuestion -> {
            val mq = MergedQuestion(
                id = newUserQuestionId(),
                questionText = event.questionText.trim(),
                answerText = event.answerText.trim(),
                difficulty = event.difficulty,
                technologyId = event.theme.technologyId,
                categoryId = event.theme.categoryId,
                themeId = event.theme.themeId,
                themeTitle = event.theme.themeTitle,
                fromBundle = false,
            )
            val overlay = state.overlay.copy(addedQuestions = state.overlay.addedQuestions + mq)
            var stackIds = fillStackIds(state.bundleThemes, overlay, state.stackIds)
            InterviewSessionReduction(
                state = state.copy(overlay = overlay, stackIds = stackIds),
                persistOverlay = true,
            )
        }

        InterviewSessionEvent.ConfirmRemoveTopCard -> {
            val id = state.stackIds.firstOrNull()
                ?: return InterviewSessionReduction(state, persistOverlay = false)
            val merged = state.mergedPool()
            val q = merged.find { it.id == id }
                ?: return InterviewSessionReduction(state, persistOverlay = false)
            var overlay = if (q.fromBundle) {
                state.overlay.copy(removedIds = state.overlay.removedIds + id)
            } else {
                state.overlay.copy(addedQuestions = state.overlay.addedQuestions.filter { it.id != id })
            }
            var stackIds = state.stackIds.filter { it != id }
            stackIds = fillStackIds(state.bundleThemes, overlay, stackIds)
            val bump = bumpShownForTopIfAny(state.bundleThemes, overlay, stackIds)
            overlay = bump.first
            InterviewSessionReduction(
                state = state.copy(overlay = overlay, stackIds = stackIds),
                persistOverlay = true,
            )
        }
    }
}
