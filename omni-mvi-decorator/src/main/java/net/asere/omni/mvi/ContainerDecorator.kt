package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

open class ContainerDecorator<UiState, SideEffect, UiAction>(
    internal val container: Container<UiState, SideEffect, UiAction>
) : StateContainer<UiState, SideEffect, UiAction> {

    override val onAction: (UiAction) -> Unit = container.onAction
    override val coroutineScope: CoroutineScope = container.coroutineScope
    override val coroutineExceptionHandler: CoroutineExceptionHandler =
        container.coroutineExceptionHandler
    override val uiState: StateFlow<UiState> = container.asStateContainer().uiState
    override val uiEffect: Flow<SideEffect> = container.asStateContainer().uiEffect
    override fun update(function: UiState.() -> UiState) =
        container.asStateContainer().update(function)

    override fun post(effect: SideEffect) = container.asStateContainer().post(effect)
}