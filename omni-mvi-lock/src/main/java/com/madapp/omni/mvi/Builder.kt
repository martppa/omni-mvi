package com.madapp.omni.mvi

fun <UiState, SideEffect, UiAction> lockContainer(
    container: Container<UiState, SideEffect, UiAction>
) = LockContainer(container)