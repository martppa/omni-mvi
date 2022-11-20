package net.asere.omni.mvi

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

fun <State, Effect, Action>
        StateContainerHost<State, Effect, Action>.stateContainer(
    initialState: State,
    onAction: (Action) -> Unit = {},
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = CoreContainer<State, Effect, Action>(
    initialState,
    onAction,
    coroutineScope,
    coroutineExceptionHandler
)