package ru.towich.achline.domain

enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD,
    ;

    val labelRu: String
        get() = when (this) {
            EASY -> "Лёгкий"
            MEDIUM -> "Средний"
            HARD -> "Сложный"
        }
}
