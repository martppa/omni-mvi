package net.asere.omni.mvi

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asQueueContainer() =
    seek<QueueContainer<UiState, SideEffect, UiAction>> { it is QueueContainer<*, *, *> }