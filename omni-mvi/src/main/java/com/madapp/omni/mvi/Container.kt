package com.madapp.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

interface Container<UiState, SideEffect, UiAction> {
    val onAction: (UiAction) -> Unit
    val coroutineScope: CoroutineScope
    val coroutineExceptionHandler: CoroutineExceptionHandler
}

fun <UiState, SideEffect, UiAction> Container<UiState, SideEffect, UiAction>
        .asStateContainer() = this as StateContainer