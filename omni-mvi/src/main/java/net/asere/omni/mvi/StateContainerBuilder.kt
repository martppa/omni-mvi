package net.asere.omni.mvi

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

fun <State, Effect>
        StateContainerHost<State, Effect>.stateContainer(
    initialState: State,
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = StateEmitterContainer<State, Effect>(
    initialState,
    coroutineScope,
    coroutineExceptionHandler
).decorate { DelegatorContainer(it) }