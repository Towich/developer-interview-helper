package ru.towich.achline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ru.towich.achline.data.AchlineAndroidContext
import ru.towich.achline.data.InterviewRepositoryImpl
import ru.towich.achline.data.createOverlayFileStorage
import ru.towich.achline.presentation.LocalInterviewRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AchlineAndroidContext.init(this)
        val repository = InterviewRepositoryImpl(createOverlayFileStorage())

        setContent {
            CompositionLocalProvider(LocalInterviewRepository provides repository) {
                App()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val context = LocalContext.current
    val repository = remember(context) {
        AchlineAndroidContext.init(context.applicationContext)
        InterviewRepositoryImpl(createOverlayFileStorage())
    }
    CompositionLocalProvider(LocalInterviewRepository provides repository) {
        App()
    }
}