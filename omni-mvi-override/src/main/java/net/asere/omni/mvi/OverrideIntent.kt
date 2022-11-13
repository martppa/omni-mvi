package net.asere.omni.mvi

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        OverrideContainerHost<UiState, SideEffect, UiAction>.overrideIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asOverrideContainer().overrideIntent(intentId, block)