package ru.towich.achline.data

import achline.composeapp.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.towich.achline.data.dto.InterviewIndexDto
import ru.towich.achline.data.dto.OverlayFileDto
import ru.towich.achline.data.dto.ThemeBundleDto
import ru.towich.achline.domain.ThemeBundleData
import ru.towich.achline.domain.UserOverlayState
import ru.towich.achline.domain.repository.InterviewRepository

class InterviewRepositoryImpl(
    private val overlayStorage: OverlayFileStorage,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) : InterviewRepository {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadBundleAndOverlay(resourceBasePath: String): Pair<List<ThemeBundleData>, UserOverlayState> {
        val indexBytes = Res.readBytes("$resourceBasePath/index.json")
        val index = json.decodeFromString<InterviewIndexDto>(indexBytes.decodeToString())
        val themes = index.themePaths.map { relative ->
            val path = "$resourceBasePath/$relative"
            val bytes = Res.readBytes(path)
            json.decodeFromString<ThemeBundleDto>(bytes.decodeToString()).toDomain()
        }
        val raw = overlayStorage.readTextOrNull()
        val overlay = if (raw.isNullOrBlank()) {
            UserOverlayState()
        } else {
            json.decodeFromString<OverlayFileDto>(raw).toDomain()
        }
        return themes to overlay
    }

    override fun saveOverlay(overlay: UserOverlayState) {
        val dto = overlay.toFileDto()
        val text = json.encodeToString(OverlayFileDto.serializer(), dto)
        overlayStorage.writeAtomic(text)
    }
}
