package ru.towich.achline.domain

import kotlinx.serialization.Serializable

@Serializable
enum class InterviewStackMode {
    /** Алгоритм ТЗ: выбор темы по сумме прогресса, затем вопрос внутри темы. */
    AllQuestions,

    /** Сначала вопросы с меньшим correctCount; при равенстве — больший (shown − correct). */
    LeastAnswered,
}
