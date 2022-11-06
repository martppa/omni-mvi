package net.asere.omni.mvi

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        QueueContainerHost<UiState, SideEffect, UiAction>.queueIntent(
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asQueueContainer().enqueue(block)