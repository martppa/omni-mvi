package com.madapp.omni.mvi.sample.shared.core.extension

fun Throwable.requireMessage() = message ?: "unknown"