package com.madapp.omni.mvi

fun <UiState, SideEffect, UiAction> queueContainer(
    container: Container<UiState, SideEffect, UiAction>
) = QueueContainer(container)