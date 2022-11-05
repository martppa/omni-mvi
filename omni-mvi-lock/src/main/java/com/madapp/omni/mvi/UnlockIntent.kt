package com.madapp.omni.mvi

fun <UiState, SideEffect, UiAction>
        LockContainerHost<UiState, SideEffect, UiAction>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)