package com.madapp.omni.mvi

class IntentScope<UiState, SideEffect>(
    val container: StateContainer<UiState, SideEffect, *>,
    internal var errorBlock: (Throwable) -> Unit = {}
)

@StateHostDsl
fun <SideEffect> IntentScope<*, SideEffect>.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }

@StateHostDsl
fun <UiState> IntentScope<UiState, *>.postState(
    function: UiState.() -> UiState
) = container.update(function)

@StateHostDsl
fun <SideEffect> IntentScope<*, SideEffect>.postEffect(
    effect:  SideEffect
) = container.post(effect)
