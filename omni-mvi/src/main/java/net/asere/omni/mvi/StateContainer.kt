package net.asere.omni.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StateContainer<UiState, SideEffect, UiAction>
    : Container<UiState, SideEffect, UiAction> {
    val uiState: StateFlow<UiState>
    val uiEffect: Flow<SideEffect>
    fun update(function: UiState.() -> UiState)
    fun post(effect: SideEffect)
}