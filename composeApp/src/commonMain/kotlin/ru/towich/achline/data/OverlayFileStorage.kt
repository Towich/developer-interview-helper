package ru.towich.achline.data

expect fun createOverlayFileStorage(): OverlayFileStorage

interface OverlayFileStorage {
    fun readTextOrNull(): String?

    fun writeAtomic(text: String)
}
