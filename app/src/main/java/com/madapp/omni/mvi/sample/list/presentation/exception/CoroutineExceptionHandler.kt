package com.madapp.omni.mvi.sample.list.presentation.exception

import kotlinx.coroutines.CoroutineExceptionHandler

fun coroutineExceptionHandler(handler: ExceptionHandler? = null) = CoroutineExceptionHandler {
        _, throwable -> handler?.handle(throwable)
}