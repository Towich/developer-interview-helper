package ru.towich.achline.presentation.topics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TopicsStateTransitionsTest {
    @Test
    fun openQuestionAndDismissDialog() {
        val start = TopicsUiState(
            isLoading = false,
            level = TopicsLevel.Questions,
            selectedFolder = TopicsFolder.Default,
            selectedTechnologyId = "Kotlin",
            selectedCategoryId = "Core",
            selectedThemeId = "coroutines",
        )

        val afterOpen = reduceTopicsState(start, TopicsUiAction.QuestionClicked("q-1"))
        assertEquals("q-1", afterOpen.selectedQuestionIdForAnswer)

        val afterDismiss = reduceTopicsState(afterOpen, TopicsUiAction.DismissAnswerDialog)
        assertNull(afterDismiss.selectedQuestionIdForAnswer)
    }

    @Test
    fun backNavigationMovesToPreviousLevel() {
        val questionsState = TopicsUiState(
            isLoading = false,
            level = TopicsLevel.Questions,
            selectedFolder = TopicsFolder.Default,
            selectedTechnologyId = "Kotlin",
            selectedCategoryId = "Core",
            selectedThemeId = "coroutines",
        )

        val (themesState, consumedFromQuestions) = backFromTopicsState(questionsState)
        assertTrue(consumedFromQuestions)
        assertEquals(TopicsLevel.Themes, themesState.level)
        assertNull(themesState.selectedThemeId)

        val (categoriesState, consumedFromThemes) = backFromTopicsState(themesState)
        assertTrue(consumedFromThemes)
        assertEquals(TopicsLevel.Categories, categoriesState.level)
        assertNull(categoriesState.selectedCategoryId)

        val (technologiesState, consumedFromCategories) = backFromTopicsState(categoriesState)
        assertTrue(consumedFromCategories)
        assertEquals(TopicsLevel.Technologies, technologiesState.level)

        val (foldersState, consumedFromTechnologies) = backFromTopicsState(technologiesState)
        assertTrue(consumedFromTechnologies)
        assertEquals(TopicsLevel.Folders, foldersState.level)
        assertNull(foldersState.selectedFolder)

        val (_, consumedFromRoot) = backFromTopicsState(foldersState)
        assertFalse(consumedFromRoot)
    }
}
