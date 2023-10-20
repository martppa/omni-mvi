package net.asere.omni.mvi

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import net.asere.omni.core.ContainerHost
import net.asere.omni.core.OmniHostDsl
import net.asere.omni.core.execute

interface StateContainerHost<State, Effect> : ContainerHost {
    override val container: ExposedStateContainer<State, Effect>
}

val <State> StateContainerHost<State, *>.currentState: State
    get() = container.asStateContainer().state.value

@OmniHostDsl
fun <State, Effect> StateContainerHost<State, Effect>.intent(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend IntentScope<State, Effect>.() -> Unit
): Job {
    val scope = IntentScope(container.asStateContainer())
    return execute(
        context = context,
        start = start,
        scope = scope,
        block = { scope.block() }
    )
}

fun <State> StateContainerHost<State, *>.observeState(onState: (State) -> Unit) = intent {
    container.state.collect { onState(it) }
}

fun <Effect> StateContainerHost<*, Effect>.observeEffect(onEffect: (Effect) -> Unit) = intent {
    container.effect.collect { onEffect(it) }
}