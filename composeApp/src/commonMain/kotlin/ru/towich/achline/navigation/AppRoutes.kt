package ru.towich.achline.navigation

import kotlinx.serialization.Serializable
import ru.towich.achline.domain.InterviewStackMode

@Serializable
data object InterviewCategoriesRoute

@Serializable
data class InterviewSessionRoute(val mode: InterviewStackMode)

@Serializable
data object TopicsRoute
