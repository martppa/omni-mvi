package net.asere.omni.mvi.shared.test

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import net.asere.omni.core.EmptyCoroutineExceptionHandler
import net.asere.omni.mvi.InnerStateContainer
import net.asere.omni.mvi.StateContainerHost
import net.asere.omni.mvi.stateContainer

fun <State : Any, Effect : Any> stateContainerHost(
    initialState: State,
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = object : StateContainerHost<State, Effect> {
    override val container = stateContainer(
        initialState = initialState,
        coroutineScope = coroutineScope,
        coroutineExceptionHandler = coroutineExceptionHandler
    )
}

fun <State : Any, Effect : Any> stateContainerHost(
    stateContainer: InnerStateContainer<State, Effect>
) = object : StateContainerHost<State, Effect> {
    override val container = stateContainer
}

fun anyStateContainerHost(
    initialState: Any = Unit,
) = object : StateContainerHost<Any, Any> {
    override val container = stateContainer(
        initialState = initialState
    )
}