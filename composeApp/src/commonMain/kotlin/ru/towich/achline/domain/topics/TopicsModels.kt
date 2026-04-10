package ru.towich.achline.domain.topics

import ru.towich.achline.domain.MergedQuestion

data class TopicsStats(
    val shownCount: Int,
    val correctCount: Int,
)

data class TopicsQuestionItem(
    val question: MergedQuestion,
    val stats: TopicsStats,
)

data class ThemeNode(
    val id: String,
    val title: String,
    val stats: TopicsStats,
    val questions: List<TopicsQuestionItem>,
)

data class CategoryNode(
    val id: String,
    val stats: TopicsStats,
    val themes: List<ThemeNode>,
)

data class TechnologyNode(
    val id: String,
    val stats: TopicsStats,
    val categories: List<CategoryNode>,
)

data class TopicsTree(
    val technologies: List<TechnologyNode>,
)
