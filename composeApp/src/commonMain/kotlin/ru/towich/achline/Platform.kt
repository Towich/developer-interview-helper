package ru.towich.achline

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform