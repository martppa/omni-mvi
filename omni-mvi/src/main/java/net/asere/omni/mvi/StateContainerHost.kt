package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

interface StateContainerHost<State, Effect, Action> {
    val container: Container<State, Effect, Action>
}

@StateHostDsl
fun <State, Effect> StateContainerHost<State, Effect, *>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<State, Effect>.() -> Unit
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

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.on(action: Action) {
    container.onAction(action)
}

private fun <T> Result<T>.onCoroutineFailure(block: (Throwable) -> Unit): Result<T> {
    onFailure {
        if (it is CancellationException) throw it
        block(it)
    }
    return this
}