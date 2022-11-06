package com.madapp.omni.mvi

internal fun <UiState, SideEffect, UiAction>
        Container<UiState, SideEffect, UiAction>.asLockContainer() =
    seek<LockContainer<UiState, SideEffect, UiAction>> { it is LockContainer<*, *, *> }