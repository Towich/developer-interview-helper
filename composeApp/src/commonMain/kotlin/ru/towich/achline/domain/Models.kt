package ru.towich.achline.domain

data class Progress(
    val correctCount: Int = 0,
    val shownCount: Int = 0,
)

data class MergedQuestion(
    val id: String,
    val questionText: String,
    val answerText: String,
    val difficulty: QuestionDifficulty,
    val technologyId: String,
    val categoryId: String,
    val themeId: String,
    val themeTitle: String,
    val fromBundle: Boolean,
)

data class ThemeBundleData(
    val technologyId: String,
    val categoryId: String,
    val themeId: String,
    val themeTitle: String,
    val questions: List<BundleQuestion>,
)

data class BundleQuestion(
    val id: String,
    val questionText: String,
    val answerText: String,
    val difficulty: QuestionDifficulty,
)

data class UserOverlayState(
    val overlaySchemaVersion: Int = CURRENT_OVERLAY_SCHEMA_VERSION,
    val progress: Map<String, Progress> = emptyMap(),
    val addedQuestions: List<MergedQuestion> = emptyList(),
    val removedIds: Set<String> = emptySet(),
)

const val CURRENT_OVERLAY_SCHEMA_VERSION: Int = 1

fun UserOverlayState.progressFor(questionId: String): Progress =
    progress[questionId] ?: Progress()

fun UserOverlayState.withProgress(questionId: String, block: (Progress) -> Progress): UserOverlayState {
    val next = block(progressFor(questionId))
    return copy(progress = progress + (questionId to next))
}
