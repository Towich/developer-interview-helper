package ru.towich.achline.domain.topics

import ru.towich.achline.domain.mergeBundleWithOverlay
import ru.towich.achline.domain.repository.InterviewRepository

class GetTopicsTreeUseCase(
    private val repository: InterviewRepository,
) {
    suspend operator fun invoke(): TopicsTree {
        val (themes, overlay) = repository.loadBundleAndOverlay()
        val merged = mergeBundleWithOverlay(themes, overlay)
        return buildTopicsTree(questions = merged, overlay = overlay)
    }
}
