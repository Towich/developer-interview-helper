package ru.towich.achline.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.towich.achline.domain.InterviewStackMode
import ru.towich.achline.domain.QuestionDifficulty

class CardSelectionTest {

    private fun q(
        id: String,
        themeId: String = "th",
    ) = MergedQuestion(
        id = id,
        questionText = "q",
        answerText = "a",
        difficulty = QuestionDifficulty.MEDIUM,
        technologyId = "t",
        categoryId = "c",
        themeId = themeId,
        themeTitle = "T",
        fromBundle = true,
    )

    @Test
    fun selectNext_prefersLowerCorrectCount() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf("a" to Progress(1, 0), "b" to Progress(0, 0))
        assertEquals("b", selectNextQuestion(pool, progress, emptySet())?.id)
    }

    @Test
    fun selectNext_tieBreakerShownCount() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf("a" to Progress(0, 5), "b" to Progress(0, 1))
        assertEquals("b", selectNextQuestion(pool, progress, emptySet())?.id)
    }

    @Test
    fun selectNext_tieBreakerId() {
        val pool = listOf(q("z"), q("m"))
        val progress = mapOf("z" to Progress(0, 0), "m" to Progress(0, 0))
        assertEquals("m", selectNextQuestion(pool, progress, emptySet())?.id)
    }

    @Test
    fun selectNext_respectsExcluded() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf("a" to Progress(0, 0), "b" to Progress(0, 0))
        assertEquals("b", selectNextQuestion(pool, progress, setOf("a"))?.id)
    }

    @Test
    fun selectNext_themeBySumCorrect_overAllQuestionsInTheme() {
        val pool = listOf(
            q("a1", "t1"),
            q("a2", "t1"),
            q("b1", "t2"),
        )
        val progress = mapOf(
            "a1" to Progress(2, 0),
            "a2" to Progress(2, 0),
            "b1" to Progress(0, 0),
        )
        val next = selectNextQuestion(pool, progress, emptySet())
        assertEquals("t2", next?.themeId)
    }

    @Test
    fun selectNext_leastAnswered_prefersLowerCorrectThenHigherShownMinusCorrect() {
        val pool = listOf(q("a"), q("b"), q("c"))
        val progress = mapOf(
            "a" to Progress(1, 10),
            "b" to Progress(0, 3),
            "c" to Progress(0, 5),
        )
        val next = selectNextQuestion(pool, progress, emptySet(), InterviewStackMode.LeastAnswered)
        assertEquals("c", next?.id)
    }

    @Test
    fun selectNext_leastAnswered_lowerCorrectWinsOverHigherGap() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf(
            "a" to Progress(0, 100),
            "b" to Progress(5, 5),
        )
        val next = selectNextQuestion(pool, progress, emptySet(), InterviewStackMode.LeastAnswered)
        assertEquals("a", next?.id)
    }

    @Test
    fun selectNext_leastAnswered_respectsExcluded() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf("a" to Progress(0, 0), "b" to Progress(1, 0))
        val next = selectNextQuestion(pool, progress, setOf("a"), InterviewStackMode.LeastAnswered)
        assertEquals("b", next?.id)
    }

    @Test
    fun selectNext_leastAnswered_skipsOnlyCorrectAnswers() {
        val pool = listOf(q("perfect"), q("weak"))
        val progress = mapOf(
            "perfect" to Progress(3, 3),
            "weak" to Progress(1, 5),
        )
        val next = selectNextQuestion(pool, progress, emptySet(), InterviewStackMode.LeastAnswered)
        assertEquals("weak", next?.id)
    }

    @Test
    fun selectNext_leastAnswered_allOnlyCorrect_returnsNull() {
        val pool = listOf(q("a"), q("b"))
        val progress = mapOf("a" to Progress(1, 1), "b" to Progress(2, 2))
        assertEquals(null, selectNextQuestion(pool, progress, emptySet(), InterviewStackMode.LeastAnswered))
    }
}
