package net.asere.omni.mvi

fun <UiState, SideEffect, UiAction> taskOverrideContainer(
    container: Container<UiState, SideEffect, UiAction>
) = TaskOverrideContainer(container)