package net.asere.omni.mvi

fun <UiState, SideEffect, UiAction> Container<UiState, SideEffect, UiAction>
        .asStateContainer() = this as StateContainer