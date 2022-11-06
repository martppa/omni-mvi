package com.madapp.omni.mvi

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        TaskOverrideContainerHost<UiState, SideEffect, UiAction>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asTaskOverrideContainer().overrideIntent(intentId, block)