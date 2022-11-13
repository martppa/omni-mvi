package net.asere.omni.mvi

fun <UiState, SideEffect, UiAction> overrideContainer(
    container: Container<UiState, SideEffect, UiAction>
) = OverrideContainer(container)