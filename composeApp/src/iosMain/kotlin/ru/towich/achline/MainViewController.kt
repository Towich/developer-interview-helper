package ru.towich.achline

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import ru.towich.achline.data.InterviewRepositoryImpl
import ru.towich.achline.data.createOverlayFileStorage
import ru.towich.achline.presentation.LocalInterviewRepository

fun MainViewController() = ComposeUIViewController {
    val repository = remember { InterviewRepositoryImpl(createOverlayFileStorage()) }
    CompositionLocalProvider(LocalInterviewRepository provides repository) {
        App()
    }
}