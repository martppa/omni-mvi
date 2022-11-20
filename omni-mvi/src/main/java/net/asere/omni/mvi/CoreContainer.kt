package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

open class CoreContainer<State, Effect, Action> internal constructor(
    initialState: State,
    override val onAction: (Action) -> Unit = {},
    override val coroutineScope: CoroutineScope,
    override val coroutineExceptionHandler: CoroutineExceptionHandler
): StateContainer<State, Effect, Action> {

    private val _state: MutableStateFlow<State> = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    private val _effect = Channel<Effect>(capacity = Channel.UNLIMITED)
    override val effect = _effect.receiveAsFlow()

    override fun update(function: State.() -> State) = _state.update { it.function() }
    override fun post(effect: Effect) { _effect.trySend(effect) }
}