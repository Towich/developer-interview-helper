package ru.towich.achline.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import ru.towich.achline.data.InterviewRepository

val LocalInterviewRepository = staticCompositionLocalOf<InterviewRepository> {
    error("LocalInterviewRepository не задан")
}
