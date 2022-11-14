package net.asere.omni.mvi

import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

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