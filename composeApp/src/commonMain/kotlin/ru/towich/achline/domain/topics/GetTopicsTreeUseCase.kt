package ru.towich.achline.domain.topics

import ru.towich.achline.domain.mergeBundleWithOverlay
import ru.towich.achline.domain.repository.InterviewRepository

class GetTopicsTreeUseCase(
    private val repository: InterviewRepository,
) {
    suspend operator fun invoke(resourceBasePath: String = "files/interview"): TopicsTree {
        val (themes, overlay) = repository.loadBundleAndOverlay(resourceBasePath)
        val merged = mergeBundleWithOverlay(themes, overlay)
        return buildTopicsTree(questions = merged, overlay = overlay)
    }
}
