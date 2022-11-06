package com.madapp.omni.mvi

interface TaskOverrideContainerHost<UiState, SideEffect, UiAction>
    : StateContainerHost<UiState, SideEffect, UiAction> {
    override val container: Container<UiState, SideEffect, UiAction>
}