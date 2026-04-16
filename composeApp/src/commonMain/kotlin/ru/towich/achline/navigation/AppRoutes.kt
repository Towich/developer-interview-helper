package ru.towich.achline.navigation

import kotlinx.serialization.Serializable
import ru.towich.achline.domain.InterviewStackMode

@Serializable
data object InterviewCategoriesRoute

@Serializable
enum class InterviewContentSource {
    Default,
    Borisproit,
}

@Serializable
data class InterviewModesRoute(val source: InterviewContentSource)

@Serializable
data class InterviewSessionRoute(
    val source: InterviewContentSource,
    val mode: InterviewStackMode,
    val resourceBasePath: String? = null,
)

@Serializable
data object TopicsRoute
