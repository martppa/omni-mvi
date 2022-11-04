package com.madapp.omni.mvi

interface StateContainerHost<UiState, SideEffect, UiAction> {
    val container: Container<UiState, SideEffect, UiAction>
}

val <UiState> StateContainerHost<UiState, *, *>.currentState: UiState
    get() = container.asStateContainer().uiState.value