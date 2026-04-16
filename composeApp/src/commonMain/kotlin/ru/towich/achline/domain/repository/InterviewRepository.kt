package ru.towich.achline.domain.repository

import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState

interface InterviewRepository {
    suspend fun loadBundleAndOverlay(
        resourceBasePath: String = "files/interview",
    ): Pair<List<ThemeBundleData>, UserOverlayState>

    fun saveOverlay(overlay: UserOverlayState)
}
