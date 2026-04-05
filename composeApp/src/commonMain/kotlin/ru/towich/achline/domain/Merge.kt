package ru.towich.achline.domain

fun mergeBundleWithOverlay(
    bundleThemes: List<ThemeBundleData>,
    overlay: UserOverlayState,
): List<MergedQuestion> {
    val fromBundle = bundleThemes.asSequence()
        .flatMap { theme ->
            theme.questions.asSequence().map { q ->
                MergedQuestion(
                    id = q.id,
                    questionText = q.questionText,
                    answerText = q.answerText,
                    technologyId = theme.technologyId,
                    categoryId = theme.categoryId,
                    themeId = theme.themeId,
                    themeTitle = theme.themeTitle,
                    fromBundle = true,
                )
            }
        }
        .filter { it.id !in overlay.removedIds }
        .toList()

    return fromBundle + overlay.addedQuestions
}
