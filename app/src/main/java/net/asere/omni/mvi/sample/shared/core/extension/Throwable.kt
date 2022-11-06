package net.asere.omni.mvi.sample.shared.core.extension

fun Throwable.requireMessage() = message ?: "unknown"