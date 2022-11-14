package net.asere.omni.mvi.shared.test

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import net.asere.omni.mvi.EmptyCoroutineExceptionHandler
import net.asere.omni.mvi.StateContainer
import net.asere.omni.mvi.StateContainerHost
import net.asere.omni.mvi.stateContainer

fun <UiState, SideEffect, UiAction> stateContainerHost(
    initialState: UiState,
    onAction: (UiAction) -> Unit = {},
    coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext),
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = object : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container = stateContainer(
        initialState = initialState,
        onAction = onAction,
        coroutineScope = coroutineScope,
        coroutineExceptionHandler = coroutineExceptionHandler
    )
}

fun <UiState, SideEffect, UiAction> stateContainerHost(
    stateContainer: StateContainer<UiState, SideEffect, UiAction>
) = object : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container = stateContainer
}