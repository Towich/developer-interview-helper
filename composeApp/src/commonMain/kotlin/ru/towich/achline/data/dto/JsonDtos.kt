package ru.towich.achline.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InterviewIndexDto(
    val schemaVersion: Int,
    val themePaths: List<String>,
)

@Serializable
data class ThemeBundleDto(
    val schemaVersion: Int,
    val technologyId: String,
    val categoryId: String,
    val themeId: String,
    val themeTitle: String,
    val questions: List<ThemeQuestionDto>,
)

@Serializable
data class ThemeQuestionDto(
    val id: String,
    val questionText: String,
    val answerText: String,
)

@Serializable
data class ProgressDto(
    val correctCount: Int = 0,
    val shownCount: Int = 0,
)

@Serializable
data class AddedQuestionDto(
    val id: String,
    val technologyId: String,
    val categoryId: String,
    val themeId: String,
    val themeTitle: String,
    val questionText: String,
    val answerText: String,
)

@Serializable
data class OverlayFileDto(
    val overlaySchemaVersion: Int = CURRENT_OVERLAY_FILE_SCHEMA,
    val progress: Map<String, ProgressDto> = emptyMap(),
    val addedQuestions: List<AddedQuestionDto> = emptyList(),
    val removedIds: List<String> = emptyList(),
)

const val CURRENT_OVERLAY_FILE_SCHEMA: Int = 1
