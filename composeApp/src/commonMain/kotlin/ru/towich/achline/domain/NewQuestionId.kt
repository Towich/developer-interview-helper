package ru.towich.achline.domain

import kotlin.random.Random

fun newUserQuestionId(): String =
    "u-" + Random.Default.nextLong().toULong().toString(16) + "-" + Random.Default.nextInt().toUInt().toString(16)
