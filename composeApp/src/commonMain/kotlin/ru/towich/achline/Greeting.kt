package ru.towich.achline

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello my bro, ${platform.name}!"
    }
}