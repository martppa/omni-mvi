package net.asere.omni.mvi

@StateHostDsl
fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<UiState, SideEffect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)