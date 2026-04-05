package ru.towich.achline.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class CardSelectionTest {

    private fun q(
        id: String,
        themeId: String = "th",
    ) = MergedQuestion(
        id = id,
        questionText = "q",
        answerText = "a",
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
}
