package net.asere.omni.mvi

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)