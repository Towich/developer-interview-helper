package ru.towich.achline.data

import ru.towich.achline.data.dto.ThemeBundleDto
import ru.towich.achline.domain.BundleQuestion
import ru.towich.achline.domain.ThemeBundleData

fun ThemeBundleDto.toDomain(): ThemeBundleData = ThemeBundleData(
    technologyId = technologyId,
    categoryId = categoryId,
    themeId = themeId,
    themeTitle = themeTitle,
    questions = questions.map {
        BundleQuestion(
            id = it.id,
            questionText = it.questionText,
            answerText = it.answerText,
        )
    },
)
