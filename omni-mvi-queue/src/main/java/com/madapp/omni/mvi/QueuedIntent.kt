package com.madapp.omni.mvi

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        QueueContainerHost<UiState, SideEffect, UiAction>.queuedIntent(
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asQueueContainer().enqueue(block)