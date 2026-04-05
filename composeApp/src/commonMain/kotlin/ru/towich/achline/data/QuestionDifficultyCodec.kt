package ru.towich.achline.data

import ru.towich.achline.domain.QuestionDifficulty

internal fun String.parseQuestionDifficulty(): QuestionDifficulty =
    when (lowercase()) {
        "easy" -> QuestionDifficulty.EASY
        "medium" -> QuestionDifficulty.MEDIUM
        "hard" -> QuestionDifficulty.HARD
        else -> QuestionDifficulty.MEDIUM
    }

internal fun QuestionDifficulty.toJsonToken(): String =
    when (this) {
        QuestionDifficulty.EASY -> "easy"
        QuestionDifficulty.MEDIUM -> "medium"
        QuestionDifficulty.HARD -> "hard"
    }
