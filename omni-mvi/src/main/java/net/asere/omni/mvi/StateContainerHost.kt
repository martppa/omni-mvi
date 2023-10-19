package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job

interface StateContainerHost<State, Effect> : ContainerHost {
    override val container: ExposedStateContainer<State, Effect>
}

val <State> StateContainerHost<State, *>.currentState: State
    get() = container.asStateContainer().state.value

@StateHostDsl
fun <State, Effect> StateContainerHost<State, Effect>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend StateIntentScope<State, Effect>.() -> Unit
): Job {
    val scope = StateIntentScope(container.asStateContainer())
    fun onError(throwable: Throwable) {
        scope.errorBlock(throwable)
    }
    val executableContainer = container.asStateContainer()
        .seek<ExecutableContainer> { it is ExecutableContainer }
    return executableContainer.execute(
        context = context,
        start = start,
        onError = ::onError,
    ) {
        scope.block()
    }
}

fun <State> StateContainerHost<State, *>.observeState(onState: (State) -> Unit) = intent {
    container.state.collect { onState(it) }
}

fun <Effect> StateContainerHost<*, Effect>.observeEffect(onEffect: (Effect) -> Unit) = intent {
    container.effect.collect { onEffect(it) }
}