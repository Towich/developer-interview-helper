package ru.towich.achline.domain

private fun Map<String, Progress>.p(questionId: String): Progress = this[questionId] ?: Progress()

/**
 * Выбор следующей карточки (ТЗ §6): тема с минимальной суммой correctCount по **всем** активным
 * вопросам темы; при равенстве — меньшая сумма shownCount, затем themeId. Кандидаты — темы,
 * у которых есть хотя бы один вопрос вне [excludedIds]. Внутри темы — среди доступных вопросов
 * минимум correctCount, shownCount, id.
 */
fun selectNextQuestion(
    pool: List<MergedQuestion>,
    progress: Map<String, Progress>,
    excludedIds: Set<String>,
): MergedQuestion? {
    val selectable = pool.filter { it.id !in excludedIds }
    if (selectable.isEmpty()) return null

    val themeIdsWithPick = selectable.map { it.themeId }.toSet()

    fun themeCorrectSum(themeId: String): Int =
        pool.asSequence()
            .filter { it.themeId == themeId }
            .sumOf { progress.p(it.id).correctCount }

    fun themeShownSum(themeId: String): Int =
        pool.asSequence()
            .filter { it.themeId == themeId }
            .sumOf { progress.p(it.id).shownCount }

    val bestTheme = themeIdsWithPick.minWithOrNull(
        compareBy<String>({ themeCorrectSum(it) }, { themeShownSum(it) }, { it }),
    ) ?: return null

    return selectable
        .filter { it.themeId == bestTheme }
        .minWithOrNull(
            compareBy<MergedQuestion>({ progress.p(it.id).correctCount }, { progress.p(it.id).shownCount }, { it.id }),
        )
}

fun pickStackQuestionIds(pool: List<MergedQuestion>, progress: Map<String, Progress>, depth: Int): List<String> {
    if (depth <= 0 || pool.isEmpty()) return emptyList()
    val ids = mutableListOf<String>()
    repeat(depth) {
        val next = selectNextQuestion(pool, progress, ids.toSet()) ?: return ids
        ids += next.id
    }
    return ids
}
