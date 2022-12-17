package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job

interface StateContainerHost<State, Effect> {
    val container: Container<State, Effect>
}

@StateHostDsl
fun <State, Effect> StateContainerHost<State, Effect>.intent(
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