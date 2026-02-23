package net.asere.omni.mvi.sample.list.presentation.exception

import kotlinx.coroutines.CoroutineExceptionHandler

fun interface ExceptionHandler {
    fun handle(throwable: Throwable)
}

fun ExceptionHandler.toCoroutineExceptionHandler() = CoroutineExceptionHandler { _, throwable ->
    handle(throwable)
}