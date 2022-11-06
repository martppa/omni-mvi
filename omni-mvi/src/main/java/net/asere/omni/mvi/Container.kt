package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope

interface Container<UiState, SideEffect, UiAction> {
    val onAction: (UiAction) -> Unit
    val coroutineScope: CoroutineScope
    val coroutineExceptionHandler: CoroutineExceptionHandler
}