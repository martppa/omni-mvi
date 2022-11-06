package com.madapp.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

val EmptyCoroutineExceptionHandler = CoroutineExceptionHandler { _, _ -> }

fun <UiState, SideEffect, UiAction>
        StateContainerHost<UiState, SideEffect, UiAction>.stateContainer(
    initialState: UiState,
    onAction: (UiAction) -> Unit = {},
    coroutineScope: CoroutineScope,
    coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
) = CoreContainer<UiState, SideEffect, UiAction>(
    initialState,
    onAction,
    coroutineScope,
    coroutineExceptionHandler
)