package ru.towich.achline.data

import ru.towich.achline.data.dto.AddedQuestionDto
import ru.towich.achline.data.dto.OverlayFileDto
import ru.towich.achline.data.dto.ProgressDto
import ru.towich.achline.domain.MergedQuestion
import ru.towich.achline.domain.Progress
import ru.towich.achline.domain.UserOverlayState

fun OverlayFileDto.toDomain(): UserOverlayState {
    val migrated = migrateOverlay(this)
    return UserOverlayState(
        overlaySchemaVersion = migrated.overlaySchemaVersion,
        progress = migrated.progress.mapValues { Progress(it.value.correctCount, it.value.shownCount) },
        addedQuestions = migrated.addedQuestions.map { it.toMergedQuestion() },
        removedIds = migrated.removedIds.toSet(),
    )
}

fun UserOverlayState.toFileDto(): OverlayFileDto = OverlayFileDto(
    overlaySchemaVersion = overlaySchemaVersion,
    progress = progress.mapValues { ProgressDto(it.value.correctCount, it.value.shownCount) },
    addedQuestions = addedQuestions.map { it.toAddedDto() },
    removedIds = removedIds.toList(),
)

private fun migrateOverlay(dto: OverlayFileDto): OverlayFileDto =
    when {
        dto.overlaySchemaVersion < 1 -> dto.copy(overlaySchemaVersion = 1)
        else -> dto
    }

private fun AddedQuestionDto.toMergedQuestion(): MergedQuestion = MergedQuestion(
    id = id,
    questionText = questionText,
    answerText = answerText,
    difficulty = difficulty.parseQuestionDifficulty(),
    technologyId = technologyId,
    categoryId = categoryId,
    themeId = themeId,
    themeTitle = themeTitle,
    fromBundle = false,
)

private fun MergedQuestion.toAddedDto(): AddedQuestionDto = AddedQuestionDto(
    id = id,
    technologyId = technologyId,
    categoryId = categoryId,
    themeId = themeId,
    themeTitle = themeTitle,
    questionText = questionText,
    answerText = answerText,
    difficulty = difficulty.toJsonToken(),
)
