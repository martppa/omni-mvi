package net.asere.omni.mvi

fun <UiState, SideEffect, UiAction> queueContainer(
    container: Container<UiState, SideEffect, UiAction>
) = QueueContainer(container)