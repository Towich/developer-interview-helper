package ru.towich.achline.domain.topics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.coroutines.startCoroutine
import ru.towich.achline.domain.BundleQuestion
import ru.towich.achline.domain.Progress
import ru.towich.achline.domain.QuestionDifficulty
import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.repository.InterviewRepository

class GetTopicsTreeUseCaseTest {
    @Test
    fun buildsTreeAndAggregatesStats() {
        val repository = FakeInterviewRepository(
            themes = listOf(
                ThemeBundleData(
                    technologyId = "Kotlin",
                    categoryId = "Core",
                    themeId = "coroutines",
                    themeTitle = "Coroutines",
                    questions = listOf(
                        BundleQuestion("q2", "Q2", "A2", QuestionDifficulty.MEDIUM),
                        BundleQuestion("q1", "Q1", "A1", QuestionDifficulty.EASY),
                    ),
                ),
            ),
            overlay = UserOverlayState(
                progress = mapOf(
                    "q1" to Progress(correctCount = 1, shownCount = 3),
                    "q2" to Progress(correctCount = 2, shownCount = 2),
                ),
            ),
        )
        val useCase = GetTopicsTreeUseCase(repository)

        val result = runSuspend { useCase() }

        val technology = result.technologies.single()
        assertEquals("Kotlin", technology.id)
        assertEquals(5, technology.stats.shownCount)
        assertEquals(3, technology.stats.correctCount)

        val category = technology.categories.single()
        assertEquals(5, category.stats.shownCount)
        assertEquals(3, category.stats.correctCount)

        val theme = category.themes.single()
        assertEquals("Coroutines", theme.title)
        assertEquals(listOf("q1", "q2"), theme.questions.map { it.question.id })
        assertEquals(5, theme.stats.shownCount)
        assertEquals(3, theme.stats.correctCount)
    }
}

private class FakeInterviewRepository(
    private val themes: List<ThemeBundleData>,
    private val overlay: UserOverlayState,
) : InterviewRepository {
    override suspend fun loadBundleAndOverlay(): Pair<List<ThemeBundleData>, UserOverlayState> = themes to overlay

    override fun saveOverlay(overlay: UserOverlayState) = Unit
}

private fun <T> runSuspend(block: suspend () -> T): T {
    var completionResult: Result<T>? = null
    block.startCoroutine(
        object : kotlin.coroutines.Continuation<T> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                completionResult = result
            }
        },
    )
    return completionResult!!.getOrThrow()
}
