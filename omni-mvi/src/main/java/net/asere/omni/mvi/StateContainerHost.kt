package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job

interface StateContainerHost<State, Effect, Action> {
    val container: Container<State, Effect, Action>
}

@StateHostDsl
fun <State, Effect> StateContainerHost<State, Effect, *>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<State, Effect>.() -> Unit
): Job {
    val scope = IntentScope(container as StateContainer)
    fun onError(throwable: Throwable) {
        scope.errorBlock(throwable)
    }
    val executableContainer = container.seek<ExecutableContainer> { it is ExecutableContainer }
    return executableContainer.execute(
        context = context,
        start = start,
        onError = ::onError,
    ) {
        scope.block()
    }
}

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.on(action: Action) {
    container.onAction(action)
}