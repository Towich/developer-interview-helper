package ru.towich.achline.domain.topics

import ru.towich.achline.domain.MergedQuestion
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.progressFor

fun buildTopicsTree(
    questions: List<MergedQuestion>,
    overlay: UserOverlayState,
): TopicsTree {
    val technologies = questions
        .groupBy { it.technologyId }
        .map { (technologyId, technologyQuestions) ->
            val categories = technologyQuestions
                .groupBy { it.categoryId }
                .map { (categoryId, categoryQuestions) ->
                    val themes = categoryQuestions
                        .groupBy { it.themeId }
                        .map { (themeId, themeQuestions) ->
                            val questionItems = themeQuestions
                                .sortedBy { it.id }
                                .map { question ->
                                    TopicsQuestionItem(
                                        question = question,
                                        stats = overlay.progressFor(question.id).toStats(),
                                    )
                                }
                            ThemeNode(
                                id = themeId,
                                title = themeQuestions.firstOrNull()?.themeTitle.orEmpty(),
                                stats = questionItems.sumStats { it.stats },
                                questions = questionItems,
                            )
                        }
                        .sortedWith(compareBy<ThemeNode> { it.title }.thenBy { it.id })

                    CategoryNode(
                        id = categoryId,
                        stats = themes.sumStats { it.stats },
                        themes = themes,
                    )
                }
                .sortedBy { it.id }

            TechnologyNode(
                id = technologyId,
                stats = categories.sumStats { it.stats },
                categories = categories,
            )
        }
        .sortedBy { it.id }

    return TopicsTree(technologies = technologies)
}

private fun <T> List<T>.sumStats(selector: (T) -> TopicsStats): TopicsStats =
    fold(TopicsStats(shownCount = 0, correctCount = 0)) { acc, item ->
        val stats = selector(item)
        TopicsStats(
            shownCount = acc.shownCount + stats.shownCount,
            correctCount = acc.correctCount + stats.correctCount,
        )
    }

private fun ru.towich.achline.domain.Progress.toStats(): TopicsStats =
    TopicsStats(shownCount = shownCount, correctCount = correctCount)
