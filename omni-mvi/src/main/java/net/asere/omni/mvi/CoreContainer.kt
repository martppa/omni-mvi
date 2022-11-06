package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

open class CoreContainer<UiState, SideEffect, UiAction> internal constructor(
    initialState: UiState,
    override val onAction: (UiAction) -> Unit = {},
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler
): StateContainer<UiState, SideEffect, UiAction> {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(initialState)
    override val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<SideEffect>(capacity = Channel.UNLIMITED)
    override val uiEffect = _uiEffect.receiveAsFlow()

    override fun update(function: UiState.() -> UiState) = _uiState.update { it.function() }
    override fun post(effect: SideEffect) { _uiEffect.trySend(effect) }
}