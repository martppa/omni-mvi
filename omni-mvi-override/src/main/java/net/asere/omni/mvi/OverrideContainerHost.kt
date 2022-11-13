package net.asere.omni.mvi

interface OverrideContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}