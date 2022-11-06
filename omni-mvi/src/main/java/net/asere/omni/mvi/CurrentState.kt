package net.asere.omni.mvi

val <UiState> StateContainerHost<UiState, *, *>.currentState: UiState
    get() = container.asStateContainer().uiState.value