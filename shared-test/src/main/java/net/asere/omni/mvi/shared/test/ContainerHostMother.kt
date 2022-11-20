package net.asere.omni.mvi.shared.test

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import net.asere.omni.mvi.EmptyCoroutineExceptionHandler
import net.asere.omni.mvi.StateContainer
import net.asere.omni.mvi.StateContainerHost
import net.asere.omni.mvi.stateContainer

fun <State, Effect, Action> stateContainerHost(
    initialState: State,
    onAction: (Action) -> Unit = {},
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = object : StateContainerHost<State, Effect, Action> {
    override val container = stateContainer(
        initialState = initialState,
        onAction = onAction,
        coroutineScope = coroutineScope,
        coroutineExceptionHandler = coroutineExceptionHandler
    )
}

fun <State, Effect, Action> stateContainerHost(
    stateContainer: StateContainer<State, Effect, Action>
) = object : StateContainerHost<State, Effect, Action> {
    override val container = stateContainer
}

fun anyStateContainerHost(
    initialState: Any = Unit,
) = object : StateContainerHost<Any, Any, Any> {
    override val container = stateContainer(
        initialState = initialState
    )
}