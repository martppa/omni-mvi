package com.madapp.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

interface StateContainerHost<UiState, SideEffect, UiAction> {
    val container: Container<UiState, SideEffect, UiAction>
}

@StateHostDsl
fun <UiState, SideEffect> StateContainerHost<UiState, SideEffect, *>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.coroutineScope.launch(
    context = context + container.coroutineExceptionHandler,
    start = start
) {
    val scope = IntentScope(container as StateContainer)
    runCatching {
        scope.block()
    }.onCoroutineFailure {
        container.coroutineExceptionHandler.handleException(context, it)
        scope.errorBlock(it)
    }
}

fun <UiState, SideEffect, UiAction>
        StateContainerHost<UiState, SideEffect, UiAction>.on(action: UiAction) {
    container.onAction(action)
}

fun <T> Result<T>.onCoroutineFailure(block: (Throwable) -> Unit): Result<T> {
    onFailure {
        if (it is CancellationException) throw it
        block(it)
    }
    return this
}