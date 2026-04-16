package ru.towich.achline.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import ru.towich.achline.domain.InterviewStackMode

/**
 * На iOS/KMP [androidx.navigation.serialization] не подставляет NavType для enum ([parseEnum] = UNKNOWN),
 * поэтому тип аргумента нужно явно передать в [typeMap] у [androidx.navigation.compose.NavHost] и
 * `composable<InterviewSessionRoute>`.
 */
private object InterviewStackModeNavType : NavType<InterviewStackMode>(false) {

    override val name: String get() = "string"

    override fun put(bundle: SavedState, key: String, value: InterviewStackMode) {
        bundle.write { putString(key, value.name) }
    }

    override fun get(bundle: SavedState, key: String): InterviewStackMode? {
        return bundle.read {
            if (!contains(key) || isNull(key)) null
            else getString(key).let { name -> InterviewStackMode.entries.firstOrNull { it.name == name } }
        }
    }

    override fun parseValue(value: String): InterviewStackMode {
        return InterviewStackMode.entries.firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown InterviewStackMode: $value")
    }

    override fun serializeAsValue(value: InterviewStackMode): String = value.name
}

private object InterviewContentSourceNavType : NavType<InterviewContentSource>(false) {
    override val name: String get() = "string"

    override fun put(bundle: SavedState, key: String, value: InterviewContentSource) {
        bundle.write { putString(key, value.name) }
    }

    override fun get(bundle: SavedState, key: String): InterviewContentSource? {
        return bundle.read {
            if (!contains(key) || isNull(key)) null
            else getString(key).let { name -> InterviewContentSource.entries.firstOrNull { it.name == name } }
        }
    }

    override fun parseValue(value: String): InterviewContentSource {
        return InterviewContentSource.entries.firstOrNull { it.name == value }
            ?: throw IllegalArgumentException("Unknown InterviewContentSource: $value")
    }

    override fun serializeAsValue(value: InterviewContentSource): String = value.name
}

val InterviewSessionRouteTypeMap: Map<KType, NavType<*>> = mapOf(
    typeOf<InterviewStackMode>() to InterviewStackModeNavType,
    typeOf<InterviewContentSource>() to InterviewContentSourceNavType,
)
