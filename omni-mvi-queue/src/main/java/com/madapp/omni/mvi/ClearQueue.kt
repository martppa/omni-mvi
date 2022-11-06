package com.madapp.omni.mvi

fun <UiState, SideEffect, UiAction>
        QueueContainerHost<UiState, SideEffect, UiAction>.clearQueue() =
    container.asQueueContainer().clearQueue()