package com.madapp.omni.mvi

fun<UiState, SideEffect, UiAction> Container<UiState, SideEffect, UiAction>.decorate(
    block: (
        Container<UiState, SideEffect, UiAction>
    ) -> Container<UiState, SideEffect, UiAction>
): Container<UiState, SideEffect, UiAction> {
    return block(this)
}