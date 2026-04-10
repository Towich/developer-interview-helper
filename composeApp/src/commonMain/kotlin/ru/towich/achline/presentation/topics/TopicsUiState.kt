package ru.towich.achline.presentation.topics

import ru.towich.achline.domain.topics.CategoryNode
import ru.towich.achline.domain.topics.TechnologyNode
import ru.towich.achline.domain.topics.ThemeNode
import ru.towich.achline.domain.topics.TopicsQuestionItem
import ru.towich.achline.domain.topics.TopicsTree

enum class TopicsLevel {
    Technologies,
    Categories,
    Themes,
    Questions,
}

data class TopicsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val tree: TopicsTree = TopicsTree(emptyList()),
    val level: TopicsLevel = TopicsLevel.Technologies,
    val selectedTechnologyId: String? = null,
    val selectedCategoryId: String? = null,
    val selectedThemeId: String? = null,
    val selectedQuestionIdForAnswer: String? = null,
) {
    val technologies: List<TechnologyNode>
        get() = tree.technologies

    val selectedTechnology: TechnologyNode?
        get() = selectedTechnologyId?.let { id -> tree.technologies.find { it.id == id } }

    val categories: List<CategoryNode>
        get() = selectedTechnology?.categories.orEmpty()

    val selectedCategory: CategoryNode?
        get() = selectedCategoryId?.let { id -> categories.find { it.id == id } }

    val themes: List<ThemeNode>
        get() = selectedCategory?.themes.orEmpty()

    val selectedTheme: ThemeNode?
        get() = selectedThemeId?.let { id -> themes.find { it.id == id } }

    val questions: List<TopicsQuestionItem>
        get() = selectedTheme?.questions.orEmpty()

    val selectedQuestion: TopicsQuestionItem?
        get() = selectedQuestionIdForAnswer?.let { id -> questions.find { it.question.id == id } }
}
